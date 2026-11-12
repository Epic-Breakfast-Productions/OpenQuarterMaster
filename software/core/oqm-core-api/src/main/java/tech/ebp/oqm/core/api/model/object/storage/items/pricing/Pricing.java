package tech.ebp.oqm.core.api.model.object.storage.items.pricing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.javamoney.moneta.format.CurrencyStyle;
import tech.ebp.oqm.core.api.model.object.Labeled;

import javax.money.MonetaryAmount;
import javax.money.format.AmountFormatQuery;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.time.ZonedDateTime;
import java.util.Locale;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
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

	public static String format(MonetaryAmount amount){
		if(amount == null){
			return null;
		}
		return FORMATTER.format(amount);
	}

	/**
	 * The label for this pricing. Example, Purchase price, sale price, etc
	 */
	@NonNull
	@NotNull
	@NotBlank
	private String label;

	/**
	 * When this pricing is valid as of
	 */
	private ZonedDateTime asOfDate;
}
