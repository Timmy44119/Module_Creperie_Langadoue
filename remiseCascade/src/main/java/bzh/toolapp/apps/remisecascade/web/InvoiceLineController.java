package bzh.toolapp.apps.remisecascade.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class InvoiceLineController {

  /**
   * Extends existing behavior in project axelor-account to be able to manage two discounts
   * information.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void compute(ActionRequest request, ActionResponse response) throws AxelorException {

    Context context = request.getContext();
    InvoiceLineService invoiceLineService = Beans.get(InvoiceLineService.class);

    InvoiceLine invoiceLine = context.asType(InvoiceLine.class);

    if (context.getParent().getContextClass() == InvoiceLine.class) {
      context = request.getContext().getParent();
    }

    Invoice invoice = this.getInvoice(context);

    if (invoice == null
        || invoiceLine.getPrice() == null
        || invoiceLine.getInTaxPrice() == null
        || invoiceLine.getQty() == null) {
      return;
    }

    BigDecimal exTaxTotal;
    BigDecimal companyExTaxTotal;
    BigDecimal inTaxTotal;
    BigDecimal companyInTaxTotal;
    BigDecimal priceDiscounted =
        invoiceLineService.computeDiscount(invoiceLine, invoice.getInAti());

    response.setValue("priceDiscounted", priceDiscounted);
    response.setAttr(
        "priceDiscounted",
        "hidden",
        priceDiscounted.compareTo(
                invoice.getInAti() ? invoiceLine.getInTaxPrice() : invoiceLine.getPrice())
            == 0);

    BigDecimal taxRate = BigDecimal.ZERO;
    if (invoiceLine.getTaxLine() != null) {
      taxRate = invoiceLine.getTaxLine().getValue();
      response.setValue("taxRate", taxRate);
      response.setValue("taxCode", invoiceLine.getTaxLine().getTax().getCode());
    }

    if (!invoice.getInAti()) {
      exTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), priceDiscounted);
      inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
    } else {
      inTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), priceDiscounted);
      exTaxTotal = inTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
    }

    companyExTaxTotal = invoiceLineService.getCompanyExTaxTotal(exTaxTotal, invoice);
    companyInTaxTotal = invoiceLineService.getCompanyExTaxTotal(inTaxTotal, invoice);

    response.setValue("exTaxTotal", exTaxTotal);
    response.setValue("inTaxTotal", inTaxTotal);
    response.setValue("companyInTaxTotal", companyInTaxTotal);
    response.setValue("companyExTaxTotal", companyExTaxTotal);
  }

  public Invoice getInvoice(Context context) {

    Context parentContext = context.getParent();

    Invoice invoice;

    if (parentContext == null
        || !parentContext.getContextClass().toString().equals(Invoice.class.toString())) {

      InvoiceLine invoiceLine = context.asType(InvoiceLine.class);

      invoice = invoiceLine.getInvoice();
    } else {
      invoice = parentContext.asType(Invoice.class);
    }

    return invoice;
  }
}
