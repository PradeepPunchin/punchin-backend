package com.punchin.utility;

import com.punchin.entity.ClaimDraftData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class CSVHelper {
    public static final String TYPE = "text/csv";

    public static boolean hasCSVFormat(MultipartFile file) {
        if (!TYPE.equals(file.getContentType()))
            return false;
        return true;
    }

    public static List<ClaimDraftData> csvToClaimsData(InputStream is, String banker) {
        CSVFormat csvFormat = CSVFormat.EXCEL.withHeader("Borrower Name", " Borrower Address", "Borrower City", " Borrower Pincode", " Borrower State", " Borrower Contact Number",
                "Borrower Email-Id", " Alternate Mobile No.", " Alternate contact details if any", " Loan Account number", " Loan Type / Category ", " Loan Disbursal Date", "Loan Disbursal Amount",
                "Lender Branch code ", "Lender Branch Address", "Lender Branch City", " Lender Branch Pin Code ", " Lender Branch State", " Lenders Local Contact Name", "Lenders Local Contact Mob Number", "Insurer Name",
                "Borrower Policy Number", " Master Pol number", " Policy Start Date ", " Policy Tenure", "  Policy Sum Assured", " Nominee Name", "Nominee relationship ", " Nominee contact number",
                "Nominee email id ", " Nominee Address ", "Claim Status", "Loan o/s amt", "Account Manager Contact Number", "Loan Account Manager Name", "Loan Amount Balance", "Loan Amount PaidBy Borrower", "Borrower Dob", "Loan Disbursal Amount");
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

             CSVParser csvParser = new CSVParser(fileReader, csvFormat.withFirstRecordAsHeader().withTrim())) {
            List<ClaimDraftData> claims = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord csvRecord : csvRecords) {
                ClaimDraftData claimsData = new ClaimDraftData();
                SimpleDateFormat formatter1 = new SimpleDateFormat("dd/MM/yyyy");
                claimsData.setBorrowerName(csvRecord.get(1));
                claimsData.setBorrowerAddress(csvRecord.get(2));
                claimsData.setBorrowerCity(csvRecord.get(3));
                claimsData.setBorrowerPinCode(csvRecord.get(4));
                claimsData.setBorrowerState(csvRecord.get(5));
                if (csvRecord.get(6).contains("+")) {
                    String borrowerContactNum = new BigDecimal(csvRecord.get(6)).toPlainString();
                    claimsData.setBorrowerContactNumber(borrowerContactNum);
                } else {
                    claimsData.setBorrowerContactNumber(csvRecord.get(6));
                }
                claimsData.setBorrowerEmailId(csvRecord.get(7));
                if (csvRecord.get(8).contains("+")) {
                    String borrowerAlternateNum = new BigDecimal(csvRecord.get(8)).toPlainString();
                    claimsData.setBorrowerAlternateContactNumber(borrowerAlternateNum);
                } else {
                    claimsData.setBorrowerAlternateContactNumber(csvRecord.get(8));
                }
                claimsData.setBorrowerAlternateContactDetails(csvRecord.get(9));
                if (csvRecord.get(10).contains("+")) {
                    String loanNumber = new BigDecimal(csvRecord.get(10)).toPlainString();
                    claimsData.setLoanAccountNumber(loanNumber);
                } else {
                    claimsData.setLoanAccountNumber(csvRecord.get(10));
                }
                claimsData.setLoanType(csvRecord.get(11));
                claimsData.setCategory(csvRecord.get(12));
                String date = csvRecord.get(13);
                if (StringUtils.isNotBlank(date)) {
                    claimsData.setLoanDisbursalDate(formatter1.parse(date));
                }
                String loanAmount = csvRecord.get(14);
                if (StringUtils.isNotBlank(loanAmount)) {
                    claimsData.setLoanAmount(NumberFormat.getNumberInstance().parse(loanAmount).doubleValue());
                }
                String loanOs = csvRecord.get(15);
                if (StringUtils.isNotBlank(loanOs)) {
                    claimsData.setLoanOutstandingAmount(NumberFormat.getNumberInstance().parse(loanOs).doubleValue());
                }
                claimsData.setBranchCode(csvRecord.get(16));
                claimsData.setBranchAddress(csvRecord.get(16));
                claimsData.setBranchCity(csvRecord.get(18));
                claimsData.setBranchPinCode(csvRecord.get(19));
                claimsData.setBranchState(csvRecord.get(20));
                claimsData.setLoanAccountManagerName(csvRecord.get(21));
                if (csvRecord.get(22).contains("+")) {
                    String accManConNum = new BigDecimal(csvRecord.get(22)).toPlainString();
                    claimsData.setAccountManagerContactNumber(accManConNum);
                } else {
                    claimsData.setAccountManagerContactNumber(csvRecord.get(22));
                }
                claimsData.setInsurerName(csvRecord.get(23));
                if (csvRecord.get(24).contains("+")) {
                    String polNum = new BigDecimal(csvRecord.get(24)).toPlainString();
                    claimsData.setPolicyNumber(polNum);
                } else {
                    claimsData.setPolicyNumber(csvRecord.get(25));
                }
                if (csvRecord.get(25).contains("+")) {
                    String masPolNum = new BigDecimal(csvRecord.get(25)).toPlainString();
                    claimsData.setMasterPolNumber(masPolNum);
                } else {
                    claimsData.setMasterPolNumber(csvRecord.get(25));
                }
                String date1 = csvRecord.get(26);
                if (StringUtils.isNotBlank(date1)) {
                    claimsData.setPolicyStartDate(formatter1.parse(date1));
                }
                String polDuration = csvRecord.get(27);
                if (StringUtils.isNotBlank(polDuration)) {
                    claimsData.setPolicyCoverageDuration(Integer.parseInt(polDuration));
                }
                String polSum = csvRecord.get(28);
                if (StringUtils.isNotBlank(polSum)) {
                    claimsData.setPolicySumAssured(NumberFormat.getNumberInstance().parse(polSum).doubleValue());
                }
                claimsData.setNomineeName(csvRecord.get(29));
                claimsData.setNomineeRelationShip(csvRecord.get(30));
                if (csvRecord.get(31).contains("+")) {
                    String nomContNum = new BigDecimal(csvRecord.get(31)).toPlainString();
                    claimsData.setNomineeContactNumber(nomContNum);
                } else {
                    claimsData.setNomineeContactNumber(csvRecord.get(31));
                }
                claimsData.setNomineeEmailId(csvRecord.get(32));
                claimsData.setNomineeAddress(csvRecord.get(33));
                claimsData.setPunchinBankerId(banker);
                claims.add(claimsData);
            }
            return claims;
        } catch (Exception e) {
            log.error("fail to parse CSV file: " + e.getMessage());
        }
        return Collections.emptyList();
    }

}