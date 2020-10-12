package com.gsas.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.gsas.exception.DatabaseException;
import com.gsas.model.BankVO;
import com.gsas.model.DocumentVO;
import com.gsas.model.LoginVO;
import com.gsas.model.SchemeApplicantDocumentsVO;
import com.gsas.model.SchemeApplicantVO;
import com.gsas.model.SchemeVO;
import com.gsas.service.SchemeService;
import com.gsas.utility.FileName;
import com.gsas.utility.LayerType;
import com.gsas.utility.ObjectFactory;

/**
 * Servlet implementation class ApplySchemeDocumentServlet
 */
@WebServlet("/ApplySchemeDocumentServlet")
@MultipartConfig
public class ApplySchemeDocumentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public ApplySchemeDocumentServlet() {
    }



	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		RequestDispatcher requestDispatcher = null;
		
		try {
			SchemeService schemeService = (SchemeService) ObjectFactory.getInstance(LayerType.SCHEME_SERVICE);
			
			HttpSession session = request.getSession();
			LoginVO loginVO = (LoginVO) session.getAttribute("loginVO");
			if(loginVO != null) {
				if(loginVO.isEmployee() == false) {
					
					SchemeApplicantDocumentsVO schemeApplicantDocuments =  null;
					SchemeApplicantVO schemeApplicantVO = new SchemeApplicantVO();
					List<SchemeApplicantDocumentsVO> applicantDocumentsdocList = new ArrayList<>();
					
					//SchemeVO schemeVO = schemeService.getSchemeDetails(Long.parseLong(request.getParameter("schemeId"))); // get scheme from schemeID
					
//					request.setParameter("schemeVO",schemeVO);
//					request.setParameter("scheme_banks",schemeVO.getBankList());
//					request.getParameter("scheme_documents",schemeVO.getDocumentList());
					
					//schemeApplicantVO Object
					System.out.println("Scheme id===>");
					//System.out.println(Long.parseLong(request.getParameter("SCHEMEID")));
					schemeApplicantVO.setSchemeVO(new SchemeVO(Long.parseLong(request.getParameter("schemeId"))));
					schemeApplicantVO.setLoginVO(loginVO);
					schemeApplicantVO.setBankVO(new BankVO(Long.parseLong(request.getParameter("bank").trim())));
					schemeApplicantVO.setAccountNumber(Long.parseLong(request.getParameter("accountNumber")));
					schemeApplicantVO.setTypeOfAccount(request.getParameter("typeOfAccount"));
					schemeApplicantVO.setIfsc(request.getParameter("ifsc"));
					schemeApplicantVO.setBranch(request.getParameter("branch"));
					
					String fileName = null;
                    List<DocumentVO> documentList = schemeService.getSchemeDocumentsList(Long.parseLong(request.getParameter("schemeId")));
                    for(DocumentVO documentVO : documentList) {
                        schemeApplicantDocuments =  new SchemeApplicantDocumentsVO();
                        schemeApplicantDocuments.setDocumentVO(documentVO);
                        
                        //Document Upload
                        Part part = request.getPart(String.valueOf(documentVO.getDocumentId()));
                        InputStream inputStream = part.getInputStream();

                        // get filename to use on the server
                        fileName = new File(FileName.extractFileName(part)).getName();
                        FileOutputStream os = new FileOutputStream ("~/Users/sukrita/Documents/Documents/Training/GSAS/WebContent/documents/"+fileName);
                        
                        // write bytes taken from uploaded file to target file
                        int ch = inputStream.read();
                        while (ch != -1) {
                             os.write(ch);
                             ch = inputStream.read();
                        }
                        os.close();
                        
                        schemeApplicantDocuments.setDocumentPath("~/Users/sukrita/Documents/Documents/Training/GSAS/WebContent/documents/"+fileName);
                        applicantDocumentsdocList.add(schemeApplicantDocuments);

                    }
                    schemeApplicantVO.setApplicantDocumentsList(applicantDocumentsdocList);
                    
                    System.out.println(schemeApplicantVO.toString());
                    
					//schemeApplicantVO.setApplicantDocumentsList(applicantDocumentsdocList);
					
					//validating documents and bank
//					for(SchemeApplicantDocumentsVO items : schemeApplicantVO.getApplicantDocumentsList()) {
//					schemeApplicantDocuments.setDocumentVO(new DocumentVO(Long.parseLong(request.getParameter("docId"))));
//					schemeApplicantDocuments.setDocumentPath(request.getParameter("docPath"));
//					applicantDocumentsdocList.add(schemeApplicantDocuments);
//					}
					
					//schemeApplicantVO = schemeService.validate(schemeApplicantVO.getSchemeVO(), schemeApplicantVO.getBankVO(), applicantDocumentsdocList, schemeApplicantVO);
					System.out.println("Approved status: "+schemeApplicantVO.isApprovedStatus());
                    if(schemeApplicantVO.isApprovedStatus() == false){  
                    	System.out.println("FALSE in if");
						request.setAttribute("err",schemeApplicantVO.getReason());
					} 
					else {
						// fill scheme_applicant table with status=true and store document
						
						System.out.println("TRUE in else");
						request.setAttribute("message","You have successfully applied for the scheme "+schemeApplicantVO.getSchemeVO().getSchemeName());
						schemeService.addSchemeApplicant(schemeApplicantVO);
					}
					requestDispatcher = request.getRequestDispatcher("viewSchemesCitizenServlet");
					requestDispatcher.forward(request, response);
				}else {													//If employee is already logged in
					requestDispatcher = request.getRequestDispatcher("viewSchemesEmployeeServlet");
					requestDispatcher.forward(request, response);
				}
					
			}
			else {
				request.setAttribute("err","Please Login First");
				requestDispatcher = request.getRequestDispatcher("citizenLogin.jsp");
				requestDispatcher.forward(request, response);
			}
			
		} catch (DatabaseException e) {
			requestDispatcher = request.getRequestDispatcher("viewSchemesCitizenServlet");
			e.printStackTrace();
			request.setAttribute("err", e.getMessage());
			requestDispatcher.forward(request, response);
		}
		
	}

}
