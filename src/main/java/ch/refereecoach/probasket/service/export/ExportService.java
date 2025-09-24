package ch.refereecoach.probasket.service.export;

import ch.refereecoach.probasket.common.CategoryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static ch.refereecoach.probasket.common.OfficiatingMode.OFFICIATING_2PO;
import static ch.refereecoach.probasket.jooq.Tables.REPORT_COMMENT;
import static ch.refereecoach.probasket.jooq.tables.Report.REPORT;
import static java.util.stream.Collectors.toMap;
import static org.apache.poi.ss.usermodel.DateUtil.getExcelDate;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final DSLContext jooqDsl;

    public void export(OutputStream out, LocalDate from, LocalDate to) throws IOException {
        try (var wb = new XSSFWorkbook()) {
            var sheet = wb.createSheet();
            wb.setSheetName(0, "Export");

            var creationHelper = wb.getCreationHelper();
            var dateCellStyle = wb.createCellStyle();
            dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd.MM.yyyy"));

            var rowIndex = 0;
            var headerRow = sheet.createRow(rowIndex++);

            var gameSummaries = findForExport(from, to);
            var categoryTypes = gameSummaries.stream()
                                             .flatMap(it -> it.scoresPerType().keySet().stream())
                                             .filter(CategoryType::isScoreRequired)
                                             .distinct().sorted().toList();

            var columnIndex = 0;
            headerRow.createCell(columnIndex++).setCellValue("Date");
            headerRow.createCell(columnIndex++).setCellValue("Game Number");
            headerRow.createCell(columnIndex++).setCellValue("Competition");
            headerRow.createCell(columnIndex++).setCellValue("Teams");
            headerRow.createCell(columnIndex++).setCellValue("Coach");
            headerRow.createCell(columnIndex++).setCellValue("Referee");
            headerRow.createCell(columnIndex++).setCellValue("Overall Score");
            for (var categoryType : categoryTypes) {
                headerRow.createCell(columnIndex++).setCellValue(categoryType.getDescription().apply(OFFICIATING_2PO));
            }

            for (var gameSummary : gameSummaries) {
                var row = sheet.createRow(rowIndex++);
                columnIndex = 0;

                var dateCell = row.createCell(columnIndex++);
                dateCell.setCellValue(getExcelDate(gameSummary.date()));
                dateCell.setCellStyle(dateCellStyle);

                row.createCell(columnIndex++).setCellValue(gameSummary.gameNumber());
                row.createCell(columnIndex++).setCellValue(gameSummary.competition());
                row.createCell(columnIndex++).setCellValue(gameSummary.teams());
                row.createCell(columnIndex++).setCellValue(gameSummary.coach());
                row.createCell(columnIndex++).setCellValue(gameSummary.referee());
                row.createCell(columnIndex++).setCellValue(gameSummary.overallScore().doubleValue());

                for (var categoryType : categoryTypes) {
                    var cell = row.createCell(columnIndex++);
                    if (gameSummary.scoresPerType().containsKey(categoryType)) {
                        cell.setCellValue(gameSummary.scoresPerType().get(categoryType).doubleValue());
                    }
                }
            }

            for (int i = 0; i < columnIndex; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
        }
    }

    private List<GameSummary> findForExport(LocalDate from, LocalDate to) {
        return jooqDsl.select(REPORT,
                              multiset(
                                      select(REPORT_COMMENT.TYPE,
                                             REPORT_COMMENT.SCORE)
                                              .from(REPORT_COMMENT)
                                              .where(REPORT_COMMENT.REPORT_ID.eq(REPORT.ID))
                                              .orderBy(REPORT_COMMENT.ID.asc())
                                      ).convertFrom(it -> it.map(mapping(CommentScore::of)))
                             )
                      .from(REPORT)
                      .where(REPORT.FINISHED_AT.isNotNull(),
                             REPORT.GAME_DATE.ge(from),
                             REPORT.GAME_DATE.le(to))
                      .orderBy(REPORT.GAME_DATE.desc(), REPORT.GAME_NUMBER.desc(), REPORT.REPORTEE_NAME.desc())
                      .fetch(it -> {
                          var report = it.value1();
                          var scores = it.value2().stream().collect(toMap(CommentScore::commentType, CommentScore::score));
                          return new GameSummary(report.getGameDate(), report.getGameNumber(), report.getGameCompetition(),
                                                 "%s - %s".formatted(report.getGameHomeTeam(), report.getGameGuestTeam()),
                                                 report.getCoachName(), report.getReporteeName(), report.getOverallScore(), scores);
                      });
    }

    private record GameSummary(LocalDate date, String gameNumber, String competition, String teams, String coach, String referee, BigDecimal overallScore,
                               Map<CategoryType, BigDecimal> scoresPerType) {

    }

    private record CommentScore(CategoryType commentType, BigDecimal score) {
        public static CommentScore of(String categoryType, BigDecimal score) {
            return new CommentScore(CategoryType.valueOf(categoryType), score);
        }
    }

}
