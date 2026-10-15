package haven.pattern.report;

import haven.model.Enquiry;
import java.util.List;

/**
 * Concrete ReportGenerator (Template Method Pattern) producing an Owner Applicant Roster.
 */
public class ApplicantRosterReport extends ReportGenerator {
    private final String ownerUsername;
    private final List<Enquiry> enquiries;

    public ApplicantRosterReport(String ownerUsername, List<Enquiry> enquiries) {
        this.ownerUsername = ownerUsername;
        this.enquiries = enquiries;
    }

    @Override
    protected String buildHeaderBanner() {
        return "                          HAVEN PROPERTY MANAGEMENT                             \n" +
               "                     STUDENT APPLICANTS & ENQUIRY ROSTER                        \n";
    }

    @Override
    protected String buildMetadataSection() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Owner Handle    : @%s\n", ownerUsername));
        sb.append(String.format("Export Date     : %s\n", formatNow()));
        sb.append(String.format("Total Enquiries : %d\n", enquiries != null ? enquiries.size() : 0));
        sb.append(SEPARATOR_SINGLE).append("\n");
        return sb.toString();
    }

    @Override
    protected String buildBodyContent() {
        StringBuilder sb = new StringBuilder();
        if (enquiries != null) {
            int idx = 1;
            for (Enquiry eq : enquiries) {
                String dateStr = (eq.getCreatedAt() != null) ? dateFormat.format(eq.getCreatedAt()) : "-";
                sb.append(String.format("[%d] Ref: %s  |  Date: %s  |  Status: %s\n", idx++, eq.getEnquiryId(), dateStr, eq.getStatus()));
                sb.append(String.format("    Property    : %s\n", eq.getAccommodationName()));
                sb.append(String.format("    Student ID  : @%s\n", eq.getStudentUsername()));
                sb.append(String.format("    Phone Number: %s\n", eq.getStudentPhone()));
                sb.append(String.format("    Message     : \"%s\"\n", eq.getMessage()));
                sb.append("    --------------------------------------------------------------------\n");
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    protected String buildFooterSection() {
        return SEPARATOR_DOUBLE + "End of HAVEN Enquiries Roster Export.\n";
    }
}
