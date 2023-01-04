package com.punchin.utility;

import com.punchin.entity.ClaimDraftData;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.ClaimStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
public class CSVHelper {
    public static final String TYPE = "text/csv";
    public static boolean hasCSVFormat(MultipartFile file) {
        if (!TYPE.equals(file.getContentType()))
            return false;
        return true;
    }

    public static List<ClaimDraftData> csvToClaimsData(InputStream is) {
        CSVFormat csvFormat = CSVFormat.EXCEL.withHeader("Borrower Name", " Borrower Address", "Borrower City", " Borrower Pincode", " Borrower State", " Borrower Contact Number",
                "Borrower Email-Id", " Alternate Mobile No.", " Alternate contact details if any", " Loan Account number", " Loan Type / Category ", " Loan Disbursal Date", "Loan Disbursal Amount",
                "Lender Branch code ", "Lender Branch Address", "Lender Branch City", " Lender Branch Pin Code ", " Lender Branch State", " Lenders Local Contact Name", "Lenders Local Contact Mob Number" , "Insurer Name",
                "Borrower Policy Number", " Master Pol number", " Policy Start Date ", " Policy Tenure", "  Policy Sum Assured", " Nominee Name", "Nominee relationship ", " Nominee contact number",
                "Nominee email id ", " Nominee Address ", "Claim Status", "Loan o/s amt", "Account Manager Contact Number", "Loan Account Manager Name", "Loan Amount Balance", "Loan Amount PaidBy Borrower", "Borrower Dob", "Loan Disbursal Amount");
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

             CSVParser csvParser = new CSVParser(fileReader, csvFormat.withFirstRecordAsHeader().withTrim())) {
            List<ClaimDraftData> claims = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord csvRecord : csvRecords) {
                ClaimDraftData claimsData = new ClaimDraftData();
                SimpleDateFormat formatter1=new SimpleDateFormat("dd/MM/yyyy");
                claimsData.setBorrowerName(csvRecord.get(1));
                claimsData.setBorrowerAddress(csvRecord.get(2));
                claimsData.setBorrowerCity(csvRecord.get(3));
                claimsData.setBorrowerPinCode(csvRecord.get(4));
                claimsData.setBorrowerState(csvRecord.get(5));
                claimsData.setBorrowerContactNumber(csvRecord.get(6));
                claimsData.setBorrowerEmailId(csvRecord.get(7));
                claimsData.setBorrowerAlternateContactNumber(csvRecord.get(8));
                claimsData.setBorrowerAlternateContactDetails(csvRecord.get(9));
                claimsData.setLoanAccountNumber(csvRecord.get(10));
                claimsData.setLoanType(csvRecord.get(11));
                String date = csvRecord.get(12);
                claimsData.setLoanDisbursalDate(formatter1.parse(date));
                String loanAmount = csvRecord.get(13);
                claimsData.setLoanAmount(Double.parseDouble(loanAmount));
                String loanOs = csvRecord.get(14);
                claimsData.setLoanOutstandingAmount(Double.parseDouble(loanOs));
                claimsData.setBranchCode(csvRecord.get(15));
                claimsData.setBranchAddress(csvRecord.get(16));
                claimsData.setBranchCity(csvRecord.get(17));
                claimsData.setBranchPinCode(csvRecord.get(18));
                claimsData.setBranchState(csvRecord.get(19));
                claimsData.setLoanAccountManagerName(csvRecord.get(20));
                claimsData.setAccountManagerContactNumber(csvRecord.get(21));
                claimsData.setInsurerName(csvRecord.get(22));
                claimsData.setPolicyNumber(csvRecord.get(23));
                claimsData.setMasterPolNumber(csvRecord.get(24));
                String date1 = csvRecord.get(25);
                claimsData.setPolicyStartDate(formatter1.parse(date1));
                String polDuration = csvRecord.get(26);
                claimsData.setPolicyCoverageDuration(Integer.parseInt(polDuration));
                String polSum = csvRecord.get(27);
                claimsData.setPolicySumAssured(Double.parseDouble(polSum));
                claimsData.setNomineeName(csvRecord.get(28));
                claimsData.setNomineeRelationShip(csvRecord.get(29));
                claimsData.setNomineeContactNumber(csvRecord.get(30));
                claimsData.setNomineeEmailId(csvRecord.get(31));
                claimsData.setNomineeAddress(csvRecord.get(32));

                claims.add(claimsData);
            }
            return claims;
        } catch (Exception e) {
            log.error("fail to parse CSV file: " + e.getMessage());
        }
        return Collections.emptyList();
    }

}