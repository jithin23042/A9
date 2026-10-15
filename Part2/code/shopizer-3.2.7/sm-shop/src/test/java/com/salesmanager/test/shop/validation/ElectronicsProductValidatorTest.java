package com.salesmanager.test.shop.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.salesmanager.core.model.catalog.category.Category;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.attribute.ProductAttribute;
import com.salesmanager.core.model.catalog.product.attribute.ProductOption;
import com.salesmanager.core.model.catalog.product.attribute.ProductOptionValue;
import com.salesmanager.core.model.catalog.product.attribute.ProductOptionValueDescription;
import com.salesmanager.core.model.catalog.product.manufacturer.Manufacturer;
import com.salesmanager.shop.store.api.exception.ConversionRuntimeException;
import com.salesmanager.shop.validation.ElectronicsProductValidator;

public class ElectronicsProductValidatorTest {

	private final ElectronicsProductValidator validator = new ElectronicsProductValidator();

	@Test
	public void acceptsElectronicsWithBrandAndWarranty() {
		validator.validate(electronicsProduct("12"));
	}

	@Test
	public void rejectsElectronicsWithoutBrand() {
		Product product = electronicsProduct("12");
		product.setManufacturer(null);

		assertInvalid(product, "brand");
	}

	@Test
	public void rejectsElectronicsWithoutWarranty() {
		Product product = electronicsProduct(null);

		assertInvalid(product, "warranty_period");
	}

	@Test
	public void rejectsWarrantyOutsideDocumentedMonthRange() {
		assertInvalid(electronicsProduct("0"), "1 and 120");
		assertInvalid(electronicsProduct("121"), "1 and 120");
	}

	@Test
	public void rejectsNonNumericWarranty() {
		assertInvalid(electronicsProduct("one year"), "1 and 120");
	}

	@Test
	public void doesNotApplyElectronicsRulesToOtherCategories() {
		Product product = new Product();
		Category category = new Category();
		category.setCode("books");
		product.getCategories().add(category);

		validator.validate(product);
		assertFalse(validator.isElectronics(product));
	}

	@Test
	public void validatesUpdatedWarrantyValue() {
		Product product = electronicsProduct("24");
		validator.validate(product);

		product.setAttributes(new java.util.HashSet<>());
		product.getAttributes().add(warrantyAttribute("0"));
		assertInvalid(product, "1 and 120");
	}

	private Product electronicsProduct(String warrantyMonths) {
		Product product = new Product();
		Category category = new Category();
		category.setCode("electronics");
		product.getCategories().add(category);

		Manufacturer brand = new Manufacturer();
		brand.setCode("ACME");
		product.setManufacturer(brand);
		if (warrantyMonths != null) {
			product.getAttributes().add(warrantyAttribute(warrantyMonths));
		}
		return product;
	}

	private ProductAttribute warrantyAttribute(String warrantyMonths) {
		ProductOption option = new ProductOption();
		option.setCode("warranty_period");

		ProductOptionValue value = new ProductOptionValue();
		ProductOptionValueDescription description = new ProductOptionValueDescription();
		description.setName(warrantyMonths);
		value.getDescriptions().add(description);

		ProductAttribute attribute = new ProductAttribute();
		attribute.setProductOption(option);
		attribute.setProductOptionValue(value);
		return attribute;
	}

	private void assertInvalid(Product product, String messagePart) {
		try {
			validator.validate(product);
			fail("Expected Electronics validation to fail");
		} catch (ConversionRuntimeException e) {
			org.junit.Assert.assertTrue(e.getErrorMessage().contains(messagePart));
		}
	}
}
