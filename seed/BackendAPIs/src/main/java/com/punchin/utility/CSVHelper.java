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
        CSVFormat csvFormat = CSVFormat.EXCEL.withHeader("Borrower Name", " Borrower Address", "Borrower City", " Borrower Pincode", " Borrower State", "Borrower Contact Number",
                "Borrower EmailId", " Borrower Alternate Contact Number", " Borrower Alternate Contact Details", " Loan Account Number", "Loan Category/Type", " Loan Disbursal Date", "Loan Disbursal Amount",
                "Branch Code", "Branch Address", "Branch City", " Lender Branch Pin code", " Lender Branch State", " Lenders Contact Name",  "Insurer Name",
                "Borrower Policy Number", " Master Policy Number", " Policy StartDate", " Policy Tenure", " Policy SumAssured", " Nominee Name", "Nominee Relationship", " Nominee Contact Number",
                "Nominee EmailId", " Nominee Address", "Claim Status", "Loan Outstanding Amount", "Account Manager Contact Number", "Loan Account Manager Name", "Loan Amount Balance", "Loan Amount PaidBy Borrower", "Borrower Dob");
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

             CSVParser csvParser = new CSVParser(fileReader, csvFormat.withFirstRecordAsHeader().withTrim())) {
            List<ClaimDraftData> claims = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord csvRecord : csvRecords) {
                ClaimDraftData claimsData = new ClaimDraftData();
                String disbursalDate = csvRecord.get("Loan Disbursal Date");
                Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(disbursalDate);
                String policyStartDate = csvRecord.get("Policy StartDate");
                Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(policyStartDate);
                claimsData.setBorrowerName(csvRecord.get("Borrower Name"));
                claimsData.setBorrowerAddress(csvRecord.get("Borrower Address"));
                claimsData.setBorrowerCity(csvRecord.get("Borrower City"));
                claimsData.setBorrowerPinCode(csvRecord.get("Borrower Pincode"));
                claimsData.setBorrowerState(csvRecord.get("Borrower State"));
                claimsData.setBorrowerContactNumber(csvRecord.get("Borrower Contact Number"));
                claimsData.setBorrowerContactNumber(csvRecord.get("Borrower Alternate Contact Number"));
                claimsData.setBorrowerContactNumber(csvRecord.get("Borrower Alternate Contact Details"));
                claimsData.setLoanType(csvRecord.get("Loan Category/Type"));
                claimsData.setLoanAccountNumber(csvRecord.get("Loan Account Number"));
                claimsData.setBorrowerEmailId(csvRecord.get("Borrower EmailId"));
                claimsData.setLoanDisbursalDate(date1);
                claimsData.setBranchCode(csvRecord.get("Lender Branch Code"));
                claimsData.setBranchAddress(csvRecord.get("Lender Branch Address"));
                claimsData.setBranchPinCode(csvRecord.get("Lender Branch Pin code"));
                claimsData.setBranchState(csvRecord.get("Lender Branch State"));
                claimsData.setBranchCity(csvRecord.get("Lender Branch City"));
                claimsData.setInsurerName(csvRecord.get("Insurer Name"));
                claimsData.setMasterPolNumber(csvRecord.get("Master Policy Number"));
                claimsData.setPolicyNumber(csvRecord.get("Borrower Policy Number"));
                claimsData.setPolicyStartDate(date2);
                claimsData.setPolicyCoverageDuration(Integer.valueOf(csvRecord.get("Policy Tenure")));
                claimsData.setPolicySumAssured(Double.parseDouble(csvRecord.get("Policy SumAssured")));
                claimsData.setNomineeName(csvRecord.get("Nominee Name"));
                claimsData.setNomineeRelationShip(csvRecord.get("Nominee Relationship"));
                claimsData.setNomineeContactNumber(csvRecord.get("Nominee Contact Number"));
                claimsData.setNomineeEmailId(csvRecord.get("Nominee EmailId"));
                claimsData.setNomineeAddress(csvRecord.get("Nominee Address"));
                claims.add(claimsData);
            }
            return claims;
        } catch (Exception e) {
            log.error("fail to parse CSV file: " + e.getMessage());
        }
        return Collections.emptyList();
    }

}