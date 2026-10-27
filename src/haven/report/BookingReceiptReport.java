package haven.report;

import haven.model.Accommodation;
import haven.model.Enquiry;
import haven.model.Hostel;
import haven.model.PG;
import haven.model.Student;
import haven.model.User;

import java.util.List;

/**
 * Generates a student enquiry and booking confirmation slip.
 */
public class BookingReceiptReport extends ReportGenerator {
    private final Enquiry enquiry;
    private final Accommodation acc;
    private final User studentUser;

    public BookingReceiptReport(Enquiry enquiry, Accommodation acc, User studentUser) {
        this.enquiry = enquiry;
        this.acc = acc;
        this.studentUser = studentUser;
    }

    @Override
    protected String buildHeaderBanner() {
        return "                          HAVEN ACCOMMODATION PLATFORM                          \n" +
               "                     STUDENT ENQUIRY & BOOKING CONFIRMATION                     \n";
    }

    @Override
    protected String buildMetadataSection() {
        StringBuilder sb = new StringBuilder();
        String nowStr = formatNow();
        sb.append(String.format("Slip Reference ID : %s\n", enquiry.getEnquiryId()));
        sb.append(String.format("Generated Date    : %s\n", nowStr));
        String eqStatus = (enquiry.getStatus() != null) ? enquiry.getStatus().toUpperCase() : "PENDING";
        sb.append(String.format("Enquiry Status    : [%s]\n", eqStatus));
        sb.append(SEPARATOR_SINGLE);
        return sb.toString();
    }

    @Override
    protected String buildBodyContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("STUDENT DETAILS:\n");
        String studentName = (studentUser != null && studentUser.getFullName() != null)
                ? studentUser.getFullName()
                : enquiry.getStudentUsername();
        sb.append(String.format("  Student Name    : %s\n", studentName));
        sb.append(String.format("  Account Handle  : @%s\n", enquiry.getStudentUsername()));
        sb.append(String.format("  Contact Phone   : %s\n", enquiry.getStudentPhone()));
        if (studentUser instanceof Student) {
            String col = ((Student) studentUser).getCollegeName();
            if (col != null && !col.isEmpty()) {
                sb.append(String.format("  College / Dept  : %s\n", col));
            }
        }
        sb.append(SEPARATOR_SINGLE);
        sb.append("PROPERTY & HOSTEL DETAILS:\n");
        sb.append(String.format("  Accommodation   : %s\n", enquiry.getAccommodationName()));
        if (acc != null) {
            sb.append(String.format("  Property Type   : %s\n", (acc instanceof Hostel) ? "Approved Hostel" : "Paying Guest (PG)"));
            sb.append(String.format("  Nearby College  : %s\n", acc.getNearbyCollege()));
            sb.append(String.format("  Distance        : %.1f km from College Campus\n", acc.getDistanceKm()));
            sb.append(String.format("  Address         : %s\n", acc.getAddress().isEmpty() ? "Trivandrum, Kerala" : acc.getAddress()));
            sb.append(String.format("  Warden / Phone  : %s\n", acc.getContactPhone().isEmpty() ? "N/A" : acc.getContactPhone()));
            sb.append(String.format("  Gender Admitted : %s\n", acc.getGenderAllowed()));

            if (acc instanceof Hostel) {
                Hostel h = (Hostel) acc;
                sb.append(String.format("  Night Curfew    : %s\n", h.getCurfewTime()));
            }

            List<String> amenities = acc.getAmenities();
            if (amenities != null && !amenities.isEmpty()) {
                sb.append(String.format("  Amenities       : %s\n", String.join(", ", amenities)));
            }
        }
        sb.append(SEPARATOR_SINGLE);
        sb.append("FINANCIAL ESTIMATE:\n");
        if (acc != null) {
            sb.append(String.format("  Base Rent Rate  : Rs. %.0f / month\n", acc.getBaseRent()));
            if (acc instanceof Hostel) {
                Hostel h = (Hostel) acc;
                sb.append(String.format("  Fixed Mess Fee  : Rs. %.0f / month\n", h.getMessFee()));
                sb.append(String.format("  Total (Standard): Rs. %.0f / month\n", acc.calculateTotalMonthlyCost(1)));
            } else if (acc instanceof PG) {
                PG pg = (PG) acc;
                sb.append(String.format("  Utility Charges : Rs. %.0f / month\n", pg.getUtilityCharges()));
                sb.append(String.format("  Food Included   : %s\n", pg.isFoodIncluded() ? "Yes" : "No"));
                sb.append(String.format("  Total (Approx)  : Rs. %.0f / month\n", acc.calculateTotalMonthlyCost(1)));
            }
        } else {
            sb.append("  Rent rate based on owner confirmation.\n");
        }
        sb.append(SEPARATOR_SINGLE);
        sb.append("STUDENT MESSAGE & NOTES:\n");
        sb.append(String.format("  \"%s\"\n", enquiry.getMessage()));
        if (enquiry.getOwnerResponse() != null && !enquiry.getOwnerResponse().isEmpty()) {
            sb.append(String.format("  Warden Response : \"%s\"\n", enquiry.getOwnerResponse()));
        }
        sb.append(SEPARATOR_SINGLE);
        return sb.toString();
    }

    @Override
    protected String buildFooterSection() {
        return "CHECKLIST FOR ADMISSION / CHECK-IN:\n" +
               "  [ ] Present original College Identity Card or Allotment Memo\n" +
               "  [ ] Submit 2 Passport-size Photographs\n" +
               "  [ ] Provide Guardian Contact Details for Warden Emergency Register\n" +
               "  [ ] Deposit Security Advance as agreed with Property Warden\n" +
               SEPARATOR_DOUBLE +
               "Generated electronically by HAVEN System. Verify directly with property warden.\n";
    }
}
