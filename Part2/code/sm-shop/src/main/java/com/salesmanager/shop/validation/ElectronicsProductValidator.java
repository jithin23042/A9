package com.salesmanager.shop.validation;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.attribute.ProductAttribute;
import com.salesmanager.core.model.catalog.product.attribute.ProductOptionValueDescription;
import com.salesmanager.core.model.catalog.product.manufacturer.Manufacturer;
import com.salesmanager.shop.store.api.exception.ConversionRuntimeException;

@Component
public class ElectronicsProductValidator {

	private static final String ELECTRONICS_CATEGORY_CODE = "electronics";
	private static final String WARRANTY_OPTION_CODE = "warranty_period";
	private static final Pattern WARRANTY_MONTHS = Pattern.compile("[0-9]{1,3}");
	private static final int MIN_WARRANTY_MONTHS = 1;
	private static final int MAX_WARRANTY_MONTHS = 120;

	public boolean isElectronics(Product product) {
		return product != null && product.getCategories() != null
				&& product.getCategories().stream()
						.anyMatch(category -> ELECTRONICS_CATEGORY_CODE.equalsIgnoreCase(category.getCode()));
	}

	public void validate(Product product) {
		if (!isElectronics(product)) {
			return;
		}

		Manufacturer brand = product.getManufacturer();
		if (brand == null || StringUtils.isBlank(brand.getCode())
				|| Manufacturer.DEFAULT_MANUFACTURER.equalsIgnoreCase(brand.getCode())) {
			throw new ConversionRuntimeException("Electronics products require a brand");
		}

		if (product.getAttributes() == null) {
			throw new ConversionRuntimeException("Electronics products require exactly one warranty_period attribute");
		}

		List<ProductAttribute> warrantyAttributes = product.getAttributes().stream()
				.filter(this::isWarrantyAttribute)
				.collect(Collectors.toList());
		if (warrantyAttributes.size() != 1) {
			throw new ConversionRuntimeException("Electronics products require exactly one warranty_period attribute");
		}

		if (!hasValidWarrantyMonths(warrantyAttributes.get(0))) {
			throw new ConversionRuntimeException(
					"Warranty period must be a whole number of months between 1 and 120");
		}
	}

	private boolean isWarrantyAttribute(ProductAttribute attribute) {
		return attribute.getProductOption() != null
				&& WARRANTY_OPTION_CODE.equalsIgnoreCase(attribute.getProductOption().getCode());
	}

	private boolean hasValidWarrantyMonths(ProductAttribute warranty) {
		if (warranty.getProductOptionValue() == null
				|| CollectionUtils.isEmpty(warranty.getProductOptionValue().getDescriptions())) {
			return false;
		}
		for (ProductOptionValueDescription description : warranty.getProductOptionValue().getDescriptions()) {
			String months = description.getName();
			if (!StringUtils.isBlank(months) && WARRANTY_MONTHS.matcher(months.trim()).matches()) {
				int period = Integer.parseInt(months.trim());
				if (period >= MIN_WARRANTY_MONTHS && period <= MAX_WARRANTY_MONTHS) {
					return true;
				}
			}
		}
		return false;
	}
}
