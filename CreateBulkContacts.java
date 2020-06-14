import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CreateBulkContacts {
	
	/*Export data info*/
	// File Name of contacts
	private final static String contactsExportName = "Contacts";
	//Vcf extension for importing contacts in mobile. Do not change this extension
	private final static String contactsExportFileExtn = ".vcf";
	// File path where it has to be exported. Path should ends will "\\"
	private final static String contactsExportFilePath = ".\\GPayPOC\\"; 
	//Default contact name
	private final static String contactsExpoxtName = "XXXXX";
	
	
	/*Mobile number series info */
	// Provide last four digits whose contact is masked
	private final static String endSeriesMobileNumber = "1234"; 
	// Country Code 91 for India
	private final static String mobileNumberCountryCode = "91";
	//Mobile Number start Series digit
	private final static String startSeriesMobileNumber = "9"; 
	
	
	/*Based on the startSeriesMobileNumber variable, the max count can be 100000. 
	 If mobile devices unable to load 100000 contacts it can be split up accordingly from contactExportStartCount to contactExportEndCount.*/
	
	// contactExportStartCount values can be any number Min : 1 to max : 99999. contactExportStartCount should be lesser than contactExportEndCount
	//contactExportStartCount value specified as -1 it will provide all 100000 contacts
	private final static int contactExportStartCount = 1;
	// contactExportEndCount values can be any number 100000; contactExportEndCount should be greater than contactExportStartCount
	private final static int contactExportEndCount = 1000;
	
	
	public static void main(String[] args) {
		
		CreateBulkContacts createBulkContacts = new CreateBulkContacts();
		List<String> mobileNumberSeriesList = createBulkContacts.generateMobileNumbers(mobileNumberCountryCode, startSeriesMobileNumber, endSeriesMobileNumber);
		if(mobileNumberSeriesList != null && mobileNumberSeriesList.size() > 0){			
			createBulkContacts.generateContactsVcf(mobileNumberSeriesList,contactsExportName,contactsExportFilePath,contactsExportFileExtn);
		} else {
			System.err.println("0 Contacts to genreate VCF. Please verify inputs...");
		}
		
	}
	
	private List<String> generateMobileNumbers(String mobileCountryCode,String startSeqNumber, String endSeqNumber){
		List<String> mobileNumberSeriesList = new ArrayList<String>();
		try {
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 10; j++) {
					for (int k = 0; k < 10; k++) {
						for (int l = 0; l < 10; l++) {
							for (int m = 0; m < 10; m++) {								
								mobileNumberSeriesList.add(mobileCountryCode+startSeqNumber+i+j+k+l+m+endSeqNumber);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("An erro occured in generateMobileNumbers method "+e.getMessage());
			e.printStackTrace();
		}
		//System.out.println("mobileNumbers List Size : "+(mobileNumberSeriesList != null && mobileNumberSeriesList.size() > 0 ? mobileNumberSeriesList.size() : 0));
		return mobileNumberSeriesList;
	}
	
	private void generateContactsVcf(List<String> mobileNumberList,String exportFileName,String exportFilePath, String exportFileExtension){
		FileWriter fw = null;
		try {
			
			if(exportFileName == null || exportFilePath == null || exportFileExtension == null || "".equalsIgnoreCase(exportFileName.trim()) || "".equalsIgnoreCase(exportFilePath.trim()) || "".equalsIgnoreCase(exportFileExtension.trim())){
				System.err.println("Export data info are invalid...");
				return;
			}
			
			//Based on 
			if(mobileNumberList != null && mobileNumberList.size() > 0){
				if(contactExportStartCount > 0 ) {
					if(contactExportStartCount < contactExportEndCount && contactExportEndCount > contactExportStartCount && contactExportEndCount <= 100000) {
 						
						File exportPath = new File(exportFilePath);
						if(!exportPath.exists()){
							exportPath.mkdirs();
						}
						
						long timeInMillis = System.currentTimeMillis();
						String seriesContactName = startSeriesMobileNumber+"Series";
						String fileNamePath = exportFilePath+seriesContactName+exportFileName+timeInMillis+exportFileExtension;
 						fw = new FileWriter(fileNamePath);
						for (int i = contactExportStartCount; i <= contactExportEndCount; i++) {
							//System.out.println(""+mobileNumberList.get(i-1));
 							fw.write("BEGIN:VCARD\r\n");
							fw.write("VERSION:3.0\r\n");
							fw.write("FN:" + contactsExpoxtName + "\r\n");
							fw.write("TEL;TYPE=WORK,VOICE:" + mobileNumberList.get(i-1).trim() + "\r\n");							
							fw.write("END:VCARD\r\n");
						}
						System.out.println("Contacts created from "+contactExportStartCount+" to "+contactExportEndCount+" and exported to "+fileNamePath);
					} else {
						System.err.println("Please verify contactExportStartCount and contactExportEndCount inputs...");
					}
				}
				
 			} 
		} catch (Exception e) {
			e.printStackTrace();
		} finally { 
			if(fw != null){
				try {
					fw.close();
				} catch (IOException e) {
 					e.printStackTrace();
 					System.err.println("IOException:Unable to close the file writter..."+e.getMessage());
				}
			}
		}
		
	}
	
}
