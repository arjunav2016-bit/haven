package haven.pattern;

import haven.model.Accommodation;
import haven.model.Enquiry;
import haven.model.Review;
import haven.model.User;
import haven.pattern.report.ApplicantRosterReport;
import haven.pattern.report.BookingReceiptReport;
import haven.pattern.report.ReportGenerator;
import haven.pattern.report.SystemAnalyticsReport;

import java.awt.*;
import java.util.List;

/**
 * ReceiptGenerator acts as a Facade over the {@link ReportGenerator} Template Method
 * hierarchy while preserving backward-compatible static utility methods.
 */
public class ReceiptGenerator {

    public static String generateEnquirySlipText(Enquiry enquiry, Accommodation acc, User studentUser) {
        return new BookingReceiptReport(enquiry, acc, studentUser).generate();
    }

    public static String generateOwnerEnquiryRoster(String ownerUsername, List<Enquiry> enquiries) {
        return new ApplicantRosterReport(ownerUsername, enquiries).generate();
    }

    public static String generateSystemAnalyticsReport(List<User> users, List<Accommodation> accommodations,
                                                       List<Enquiry> enquiries, List<Review> reviews) {
        return new SystemAnalyticsReport(users, accommodations, enquiries, reviews).generate();
    }

    public static void exportToFileWithDialog(Component parent, String defaultFileName, String content, String dialogTitle) {
        ReportGenerator.exportTextToFileWithDialog(parent, defaultFileName, content, dialogTitle);
    }
}
