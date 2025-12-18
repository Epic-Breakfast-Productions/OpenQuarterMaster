package tech.ebp.oqm.core.api.model.object.storage.items.pricing;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.javamoney.moneta.format.CurrencyStyle;
import tech.ebp.oqm.core.api.model.object.Labeled;

import javax.money.MonetaryAmount;
import javax.money.format.AmountFormatQuery;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.util.Locale;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pricing implements Labeled {
	
	public static final MonetaryAmountFormat FORMATTER;
	
	static {
		//TODO:: more flexible, based on config?
		AmountFormatQuery formatQuery = AmountFormatQueryBuilder.of(Locale.US)
											.set(CurrencyStyle.SYMBOL)
//											.set("currencyStyle", "symbol")
//											.set("pattern", "¤#,##0.00")
											.build();

		FORMATTER = MonetaryFormats.getAmountFormat(formatQuery);
	}
	
	/**
	 * The label for this pricing. Example, Purchase price, sale price, etc
	 */
	@NonNull
	@NotNull
	@NotBlank
	private String label;
	
	/**
	 * The actual price described. Divide by 100 to get the price notation.
	 */
	@NonNull
	@NotNull
	private MonetaryAmount price;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getPriceString() {
//		return this.getPrice().toString();
		return FORMATTER.format(this.getPrice());
	}
}
