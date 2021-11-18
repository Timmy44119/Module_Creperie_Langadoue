package bzh.toolapp.apps.remisecascade.service;

import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.businessproduction.service.SaleOrderLineBusinessProductionServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;

import java.math.BigDecimal;

public class ExtendedSaleOrderLineServiceImpl extends SaleOrderLineBusinessProductionServiceImpl
		implements SaleOrderLineService {

	@Inject
	public ExtendedSaleOrderLineServiceImpl(CurrencyService currencyService, PriceListService priceListService,
			ProductMultipleQtyService productMultipleQtyService, AppBaseService appBaseService,
			AppSaleService appSaleService, AccountManagementService accountManagementService,
			SaleOrderLineRepository saleOrderLineRepo, AppAccountService appAccountService,
			AnalyticMoveLineService analyticMoveLineService, AppSupplychainService appSupplychainService) {
		super(currencyService, priceListService, productMultipleQtyService, appBaseService, appSaleService,
				accountManagementService, saleOrderLineRepo, appAccountService, analyticMoveLineService,
				appSupplychainService);
	}

	@Override
	public BigDecimal computeDiscount(final SaleOrderLine saleOrderLine, final Boolean inAti) {

		final BigDecimal price = inAti ? saleOrderLine.getInTaxPrice() : saleOrderLine.getPrice();

		// compute first discount
		final BigDecimal firstDiscount = this.priceListService.computeDiscount(price,
				saleOrderLine.getDiscountTypeSelect(), saleOrderLine.getDiscountAmount());
		// then second discount
		final BigDecimal secondDiscount = this.priceListService.computeDiscount(firstDiscount,
				saleOrderLine.getSecDiscountTypeSelect(), saleOrderLine.getSecDiscountAmount());

		return secondDiscount;
	}
}
