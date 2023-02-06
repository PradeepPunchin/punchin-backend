package com.punchin.service;

import com.punchin.dto.DownloadVerifierMisResponse;
import com.punchin.entity.ClaimDraftData;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.User;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.enums.RoleEnum;
import com.punchin.repository.ClaimDraftDataRepository;
import com.punchin.repository.ClaimsDataRepository;
import com.punchin.repository.UserRepository;
import com.punchin.utility.GenericUtils;
import com.punchin.utility.ObjectMapperUtils;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MISExportService {
    @Autowired
    private ClaimsDataRepository claimsDataRepository;
    @Autowired
    AmazonS3FileManagers amazonS3FileManagers;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    ClaimDraftDataRepository claimDraftDataRepository;


    public String downloadBankerExcelFile(ClaimDataFilter claimDataFilter) {
        try {
            List<ClaimsData> claimsDataList = new ArrayList<>();
            List<ClaimStatus> claimsStatus = new ArrayList<>();
            List<Long> bankerIds = new ArrayList<>();
            if (GenericUtils.getLoggedInUser().getRole().equals(RoleEnum.SUPER_BANKER)) {
                bankerIds = userRepository.getAllBankerIds();
            }
            bankerIds.add(GenericUtils.getLoggedInUser().getId());
            if (claimDataFilter.ALL.equals(claimDataFilter)) {
                claimsDataList = claimsDataRepository.findAllByBankerIdInOrderByClaimInwardDateDesc(bankerIds);
            } else if (claimDataFilter.BANKER_ACTION_PENDING.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.CLAIM_INTIMATED);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndBankerIdInOrderByClaimInwardDateDesc(claimsStatus, bankerIds);
            } else if (claimDataFilter.SUBMITTED.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.CLAIM_SUBMITTED);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndBankerIdInOrderByClaimInwardDateDesc(claimsStatus, bankerIds);
            } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.IN_PROGRESS);
                claimsStatus.add(ClaimStatus.CLAIM_SUBMITTED);
                claimsStatus.add(ClaimStatus.CLAIM_INTIMATED);
                claimsStatus.add(ClaimStatus.AGENT_ALLOCATED);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndBankerIdInOrderByClaimInwardDateDesc(claimsStatus, bankerIds);
            } else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.UNDER_VERIFICATION);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndBankerIdInOrderByClaimInwardDateDesc(claimsStatus, bankerIds);
            } else if (claimDataFilter.SETTLED.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.SETTLED);
                claimsStatus.add(ClaimStatus.SUBMITTED_TO_LENDER);
                claimsStatus.add(ClaimStatus.SUBMITTED_TO_INSURER);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndBankerIdInOrderByClaimInwardDateDesc(claimsStatus, bankerIds);
            } else if (claimDataFilter.DISCREPENCY.equals(claimDataFilter)) {
//                claimsStatus.add(ClaimStatus.VERIFIER_DISCREPENCY);
                claimsStatus.add(ClaimStatus.BANKER_DISCREPANCY);
                claimsStatus.add(ClaimStatus.NEW_REQUIREMENT);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndBankerIdInOrderByClaimInwardDateDesc(claimsStatus, bankerIds);
            }
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            String filename = "Claim_MIS_" + format.format(new Date()) + ".xlsx";
            String filePath = System.getProperty("user.dir") + "/BackendAPIs/downloads/" + filename;
            File file = new File(filePath);
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Sheet1");
            writeHeaderLine(workbook, sheet);
            writeDataLines(workbook, sheet, claimsDataList, format);
            FileOutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            workbook.close();
            String versionId = amazonS3FileManagers.uploadFileToAmazonS3("mis_upload/", file, filename);
            amazonS3FileManagers.cleanUp(file);
            return versionId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void writeHeaderLine(Workbook workbook, Sheet sheet) {
        Row row = sheet.createRow(0);
        HSSFWorkbook hwb = new HSSFWorkbook();
        HSSFPalette palette = hwb.getCustomPalette();
        HSSFColor headerBackgroundColor = palette.findSimilarColor(222, 234, 246);
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(headerBackgroundColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        style.setFont(font);
        createCell(row, 0, "S.No", style, sheet);
        createCell(row, 1, "PunchIn Ref Id", style, sheet);
        createCell(row, 2, "Case Inward date", style, sheet);
        createCell(row, 3, "Borrower Name", style, sheet);
        createCell(row, 4, "Borrower Address", style, sheet);
        createCell(row, 5, "Borrower City", style, sheet);
        createCell(row, 6, "Borrower Pin Code", style, sheet);
        createCell(row, 7, "Borrower State", style, sheet);
        createCell(row, 8, "Borrower Contact Number", style, sheet);
        createCell(row, 9, "Borrower Email id", style, sheet);
        createCell(row, 10, "Alternate Mobile No.", style, sheet);
        createCell(row, 11, "Alternate Contact Details", style, sheet);
        createCell(row, 12, "Loan Account Number", style, sheet);
        createCell(row, 13, "Loan Category/Type", style, sheet);
        createCell(row, 14, "Loan Disbursal Date", style, sheet);
        createCell(row, 15, "Loan Disbursal Amount", style, sheet);
        createCell(row, 16, "Loan O/S Amount", style, sheet);
        createCell(row, 17, "Lender Branch Code", style, sheet);
        createCell(row, 18, "Lender Branch Address", style, sheet);
        createCell(row, 19, "Lender Branch City", style, sheet);
        createCell(row, 20, "Lender Branch Pin code", style, sheet);
        createCell(row, 21, "Lender Branch State", style, sheet);
        createCell(row, 22, "Lenders Local Contact Name", style, sheet);
        createCell(row, 23, "Lenders Local Contact Mobile No", style, sheet);
        createCell(row, 24, "Insurer Name", style, sheet);
        createCell(row, 25, "Borrower Policy Number", style, sheet);
        createCell(row, 26, "Master Policy Number", style, sheet);
        createCell(row, 27, "Policy Start Date", style, sheet);
        createCell(row, 28, "Policy Tenure", style, sheet);
        createCell(row, 29, "Policy Sum Assured", style, sheet);
        createCell(row, 30, "Nominee Name", style, sheet);
        createCell(row, 31, "Nominee Relationship", style, sheet);
        createCell(row, 32, "Nominee Contact Number", style, sheet);
        createCell(row, 33, "Nominee Email id", style, sheet);
        createCell(row, 34, "Nominee Address", style, sheet);
        createCell(row, 35, "Claim Action", style, sheet);
        createCell(row, 36, "Claim Status", style, sheet);
        createCell(row, 37, "Claim Status Date", style, sheet);
        createCell(row, 38, "Verifier Mapped", style, sheet);
        createCell(row, 39, "Verifier Name", style, sheet);
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style, Sheet sheet) {
        sheet.setColumnWidth(columnCount, 50 * 120);
        Cell cell = row.createCell(columnCount);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
        cell.setCellStyle(style);
    }

    private void writeDataLines(Workbook workbook, Sheet sheet, List<ClaimsData> claimsDataList, SimpleDateFormat format) {
        int rowCount = 1;
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        style.setFont(font);
        int sno = 1;
        for (ClaimsData claimsData : claimsDataList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, sno, style, sheet);
            createCell(row, columnCount++, claimsData.getPunchinClaimId(), style, sheet);
            createCell(row, columnCount++, format.format(claimsData.getClaimInwardDate()).toString(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerName(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerAddress(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerCity(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerPinCode(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerState(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerContactNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerEmailId(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerAlternateContactNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerAlternateContactDetails(), style, sheet);
            createCell(row, columnCount++, claimsData.getLoanAccountNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getLoanType(), style, sheet);
            createCell(row, columnCount++, format.format(claimsData.getLoanDisbursalDate()), style, sheet);
            createCell(row, columnCount++, claimsData.getLoanAmount(), style, sheet);
            createCell(row, columnCount++, claimsData.getLoanOutstandingAmount(), style, sheet);
            createCell(row, columnCount++, claimsData.getBranchCode(), style, sheet);
            createCell(row, columnCount++, claimsData.getBranchAddress(), style, sheet);
            createCell(row, columnCount++, claimsData.getBranchCity(), style, sheet);
            createCell(row, columnCount++, claimsData.getBranchPinCode(), style, sheet);
            createCell(row, columnCount++, claimsData.getBranchState(), style, sheet);
            createCell(row, columnCount++, claimsData.getLoanAccountManagerName(), style, sheet);
            createCell(row, columnCount++, claimsData.getAccountManagerContactNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getInsurerName(), style, sheet);
            createCell(row, columnCount++, claimsData.getPolicyNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getMasterPolNumber(), style, sheet);
            createCell(row, columnCount++, format.format(claimsData.getPolicyStartDate()), style, sheet);
            createCell(row, columnCount++, claimsData.getPolicyCoverageDuration(), style, sheet);
            createCell(row, columnCount++, claimsData.getPolicySumAssured(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeName(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeRelationShip(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeContactNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeEmailId(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeAddress(), style, sheet);
            if (claimsData.getClaimStatus().equals(ClaimStatus.SUBMITTED_TO_LENDER)) {
                createCell(row, columnCount++, "Close", style, sheet);
            } else {
                createCell(row, columnCount++, "Open", style, sheet);
            }
            createCell(row, columnCount++, claimsData.getClaimStatus().name(), style, sheet);
            createCell(row, columnCount++, format.format(new Date()), style, sheet);
            if (Objects.nonNull(claimsData.getVerifierId()) && claimsData.getVerifierId() > 0) {
                createCell(row, columnCount++, "Yes", style, sheet);
                Optional<User> user = userRepository.findById(claimsData.getVerifierId());
                if (user.isPresent()) {
                    createCell(row, columnCount++, user.get().getFirstName() + " " + user.get().getLastName() + " (" + user.get().getUserId() + ")", style, sheet);
                } else {
                    createCell(row, columnCount++, null, style, sheet);
                }
            } else {
                createCell(row, columnCount++, "No", style, sheet);
                createCell(row, columnCount++, null, style, sheet);
            }
            sno++;
        }
    }

    public String downloadVerifierMISReport(ClaimDataFilter claimDataFilter) {
        try {
            List<ClaimsData> claimsDataList = new ArrayList<>();
            List<ClaimStatus> claimsStatus = new ArrayList<>();
            if (claimDataFilter.ALL.equals(claimDataFilter)) {
                claimsDataList = claimsDataRepository.findByVerifierIdOrderByClaimInwardDateDesc(GenericUtils.getLoggedInUser().getId());
            } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
                claimsStatus.removeAll(claimsStatus);
                claimsStatus.add(ClaimStatus.IN_PROGRESS);
                claimsStatus.add(ClaimStatus.CLAIM_SUBMITTED);
                claimsStatus.add(ClaimStatus.CLAIM_INTIMATED);
                claimsStatus.add(ClaimStatus.AGENT_ALLOCATED);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndVerifierIdOrderByClaimInwardDateDesc(claimsStatus, GenericUtils.getLoggedInUser().getId());
            } else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
                claimsStatus.removeAll(claimsStatus);
                claimsStatus.add(ClaimStatus.UNDER_VERIFICATION);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndVerifierIdOrderByClaimInwardDateDesc(claimsStatus, GenericUtils.getLoggedInUser().getId());
            } else if (claimDataFilter.SETTLED.equals(claimDataFilter)) {
                claimsStatus.removeAll(claimsStatus);
                claimsStatus.add(ClaimStatus.SETTLED);
                claimsStatus.add(ClaimStatus.SUBMITTED_TO_LENDER);
                claimsStatus.add(ClaimStatus.SUBMITTED_TO_INSURER);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndVerifierIdOrderByClaimInwardDateDesc(claimsStatus, GenericUtils.getLoggedInUser().getId());
            } else if (claimDataFilter.DISCREPENCY.equals(claimDataFilter)) {
                claimsStatus.removeAll(claimsStatus);
                claimsStatus.add(ClaimStatus.VERIFIER_DISCREPENCY);
                claimsStatus.add(ClaimStatus.BANKER_DISCREPANCY);
                claimsStatus.add(ClaimStatus.NEW_REQUIREMENT);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndVerifierIdOrderByClaimInwardDateDesc(claimsStatus, GenericUtils.getLoggedInUser().getId());
            } else if (claimDataFilter.ALLOCATED.equals(claimDataFilter)) {
                claimsStatus.removeAll(claimsStatus);
                claimsStatus.add(ClaimStatus.AGENT_ALLOCATED);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndVerifierIdOrderByClaimInwardDateDesc(claimsStatus, GenericUtils.getLoggedInUser().getId());
            }
            List<DownloadVerifierMisResponse> downloadVerifierMisResponseList = ObjectMapperUtils.mapAll(claimsDataList, DownloadVerifierMisResponse.class);
            for (DownloadVerifierMisResponse downloadVerifierMisResponse : downloadVerifierMisResponseList) {
                if (downloadVerifierMisResponse.getAgentId() > 0) {
                    downloadVerifierMisResponse.setAgentMapped("Yes");
                    Optional<User> user = userRepository.findById(downloadVerifierMisResponse.getAgentId());
                    if (user.isPresent()) {
                        downloadVerifierMisResponse.setAgentName(user.get().getFirstName() + " " + user.get().getLastName() + " (" + user.get().getUserId() + ")");
                    }
                } else {
                    downloadVerifierMisResponse.setAgentMapped("No");
                }
            }
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            String filename = "Claim_MIS_" + format.format(new Date()) + ".xlsx";
            String filePath = System.getProperty("user.dir") + "/BackendAPIs/downloads/" + filename;
            File file = new File(filePath);
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Sheet1");
            writeHeaderLineDownloadMIS(workbook, sheet);
            writeDataLinesDownloadMis(workbook, sheet, downloadVerifierMisResponseList, format);
            FileOutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            workbook.close();
            String versionId = amazonS3FileManagers.uploadFileToAmazonS3("mis_upload/", file, filename);
            amazonS3FileManagers.cleanUp(file);
            return versionId;
        } catch (Exception e) {
            return null;
        }
    }

    private void writeDataLinesDownloadMis(Workbook workbook, Sheet sheet, List<DownloadVerifierMisResponse> claimsDataList, SimpleDateFormat format) {
        int rowCount = 1;
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        style.setFont(font);
        int sno = 1;
        for (DownloadVerifierMisResponse claimsData : claimsDataList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, sno, style, sheet);
            createCell(row, columnCount++, claimsData.getPunchinClaimId(), style, sheet);
            createCell(row, columnCount++, format.format(claimsData.getClaimInwardDate()).toString(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerName(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerAddress(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerCity(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerPinCode(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerState(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerContactNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerAlternateContactNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getInsurerName(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeName(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeRelationShip(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeContactNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeAddress(), style, sheet);
            if (claimsData.getClaimStatus().equals(ClaimStatus.SUBMITTED_TO_LENDER)) {
                createCell(row, columnCount++, "Close", style, sheet);
            } else {
                createCell(row, columnCount++, "Open", style, sheet);
            }
            createCell(row, columnCount++, claimsData.getClaimStatus().name(), style, sheet);
            createCell(row, columnCount++, format.format(new Date()), style, sheet);
            createCell(row, columnCount++, claimsData.getAgentMapped(), style, sheet);
            createCell(row, columnCount++, claimsData.getAgentName(), style, sheet);
            sno++;
        }
    }

    private void writeHeaderLineDownloadMIS(Workbook workbook, Sheet sheet) {
        Row row = sheet.createRow(0);
        HSSFWorkbook hwb = new HSSFWorkbook();
        HSSFPalette palette = hwb.getCustomPalette();
        HSSFColor headerBackgroundColor = palette.findSimilarColor(222, 234, 246);
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(headerBackgroundColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        style.setFont(font);
        createCell(row, 0, "S.No", style, sheet);
        createCell(row, 1, "PunchIn Ref Id", style, sheet);
        createCell(row, 2, "Case Inward date", style, sheet);
        createCell(row, 3, "Borrower Name", style, sheet);
        createCell(row, 4, "Borrower Address", style, sheet);
        createCell(row, 5, "Borrower City", style, sheet);
        createCell(row, 6, "Borrower Pin Code", style, sheet);
        createCell(row, 7, "Borrower State", style, sheet);
        createCell(row, 8, "Borrower Contact Number", style, sheet);
        createCell(row, 9, "Alternate Mobile No.", style, sheet);
        createCell(row, 10, "Insurer Name", style, sheet);
        createCell(row, 11, "Nominee Name", style, sheet);
        createCell(row, 12, "Nominee Relationship", style, sheet);
        createCell(row, 13, "Nominee Contact Number", style, sheet);
        createCell(row, 14, "Nominee Address", style, sheet);
        createCell(row, 15, "Claim Action", style, sheet);
        createCell(row, 16, "Claim Status", style, sheet);
        createCell(row, 17, "Claim Status Date", style, sheet);
        createCell(row, 18, "Agent Mapped", style, sheet);
        createCell(row, 19, "Agent Name", style, sheet);
    }

    public String exportRejectedClaimsData(String banker) throws IOException {
        List<ClaimDraftData> claimDraftDataList = claimDraftDataRepository.findRejectedClaimDataByBankerId(banker);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        String filename = "Rejected_Claim_MIS_" + format.format(new Date()) + ".xlsx";
        String filePath = System.getProperty("user.dir") + "/BackendAPIs/downloads/" + filename;
        File file = new File(filePath);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        writeHeaderLineExportRejected(workbook, sheet);
        writeDataLinesExportReject(workbook, sheet, claimDraftDataList, format);
        FileOutputStream outputStream = new FileOutputStream(filePath);
        workbook.write(outputStream);
        workbook.close();
        String versionId = amazonS3FileManagers.uploadFileToAmazonS3("mis_upload/", file, filename);
        amazonS3FileManagers.cleanUp(file);
        return versionId;
    }

    private void writeDataLinesExportReject(Workbook workbook, Sheet sheet, List<ClaimDraftData> claimDraftDataList, SimpleDateFormat format) {
        int rowCount = 1;
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        style.setFont(font);
        int sno = 1;
        for (ClaimDraftData claimsData : claimDraftDataList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, sno, style, sheet);
            if (claimsData.getClaimInwardDate() != null) {
                createCell(row, columnCount++, format.format(claimsData.getClaimInwardDate()).toString(), style, sheet);
            } else {
                createCell(row, columnCount++, "", style, sheet);

            }
            createCell(row, columnCount++, claimsData.getBorrowerName(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerAddress(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerCity(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerPinCode(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerState(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerContactNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerEmailId(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerAlternateContactNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getBorrowerAlternateContactDetails(), style, sheet);
            createCell(row, columnCount++, claimsData.getLoanAccountNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getLoanType(), style, sheet);
            createCell(row, columnCount++, claimsData.getCategory(), style, sheet);
            if (claimsData.getLoanDisbursalDate() != null) {
                createCell(row, columnCount++, format.format(claimsData.getLoanDisbursalDate()), style, sheet);
            } else {
                createCell(row, columnCount++, null, style, sheet);
            }
            createCell(row, columnCount++, claimsData.getLoanAmount(), style, sheet);
            createCell(row, columnCount++, claimsData.getLoanOutstandingAmount(), style, sheet);
            createCell(row, columnCount++, claimsData.getBranchCode(), style, sheet);
            createCell(row, columnCount++, claimsData.getBranchAddress(), style, sheet);
            createCell(row, columnCount++, claimsData.getBranchCity(), style, sheet);
            createCell(row, columnCount++, claimsData.getBranchPinCode(), style, sheet);
            createCell(row, columnCount++, claimsData.getBranchState(), style, sheet);
            createCell(row, columnCount++, claimsData.getLoanAccountManagerName(), style, sheet);
            createCell(row, columnCount++, claimsData.getAccountManagerContactNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getInsurerName(), style, sheet);
            createCell(row, columnCount++, claimsData.getPolicyNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getMasterPolNumber(), style, sheet);
            if (claimsData.getPolicyStartDate() != null) {
                createCell(row, columnCount++, format.format(claimsData.getPolicyStartDate()), style, sheet);
            } else {
                createCell(row, columnCount++, null, style, sheet);
            }
            createCell(row, columnCount++, claimsData.getPolicyCoverageDuration(), style, sheet);
            createCell(row, columnCount++, claimsData.getPolicySumAssured(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeName(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeRelationShip(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeContactNumber(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeEmailId(), style, sheet);
            createCell(row, columnCount++, claimsData.getNomineeAddress(), style, sheet);
            createCell(row, columnCount++, claimsData.getInvalidClaimDataReason(), style, sheet);
            createCell(row, columnCount++, "", style, sheet);
            sno++;
        }
    }

    private void writeHeaderLineExportRejected(Workbook workbook, Sheet sheet) {
        Row row = sheet.createRow(0);
        HSSFWorkbook hwb = new HSSFWorkbook();
        HSSFPalette palette = hwb.getCustomPalette();
        HSSFColor headerBackgroundColor = palette.findSimilarColor(222, 234, 246);
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(headerBackgroundColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        style.setFont(font);
        createCell(row, 0, "S.No", style, sheet);
        createCell(row, 1, "Case Inward date", style, sheet);
        createCell(row, 2, "Borrower Name", style, sheet);
        createCell(row, 3, "Borrower Address", style, sheet);
        createCell(row, 4, "Borrower City", style, sheet);
        createCell(row, 5, "Borrower Pin Code", style, sheet);
        createCell(row, 6, "Borrower State", style, sheet);
        createCell(row, 7, "Borrower Contact Number", style, sheet);
        createCell(row, 8, "Borrower Email id", style, sheet);
        createCell(row, 9, "Alternate Mobile No.", style, sheet);
        createCell(row, 10, "Alternate Contact Details", style, sheet);
        createCell(row, 11, "Loan Account Number", style, sheet);
        createCell(row, 12, "Loan Category/Type", style, sheet);
        createCell(row, 13, "Insurance Type", style, sheet);
        createCell(row, 14, "Loan Disbursal Date", style, sheet);
        createCell(row, 15, "Loan Disbursal Amount", style, sheet);
        createCell(row, 16, "Loan O/S Amount", style, sheet);
        createCell(row, 17, "Lender Branch Code", style, sheet);
        createCell(row, 18, "Lender Branch Address", style, sheet);
        createCell(row, 19, "Lender Branch City", style, sheet);
        createCell(row, 20, "Lender Branch Pin code", style, sheet);
        createCell(row, 21, "Lender Branch State", style, sheet);
        createCell(row, 22, "Lenders Local Contact Name", style, sheet);
        createCell(row, 23, "Lenders Local Contact Mobile No", style, sheet);
        createCell(row, 24, "Insurer Name", style, sheet);
        createCell(row, 25, "Borrower Policy Number", style, sheet);
        createCell(row, 26, "Master Policy Number", style, sheet);
        createCell(row, 27, "Policy Start Date", style, sheet);
        createCell(row, 28, "Policy Tenure", style, sheet);
        createCell(row, 29, "Policy Sum Assured", style, sheet);
        createCell(row, 30, "Nominee Name", style, sheet);
        createCell(row, 31, "Nominee Relationship", style, sheet);
        createCell(row, 32, "Nominee Contact Number", style, sheet);
        createCell(row, 33, "Nominee Email id", style, sheet);
        createCell(row, 34, "Nominee Address", style, sheet);
        createCell(row, 35, "Reason for Rejected", style, sheet);
    }

}
