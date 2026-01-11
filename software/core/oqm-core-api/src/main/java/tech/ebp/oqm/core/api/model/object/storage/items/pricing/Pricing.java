package tech.ebp.oqm.core.api.model.object.storage.items.pricing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.javamoney.moneta.format.CurrencyStyle;
import tech.ebp.oqm.core.api.model.object.Labeled;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinFullTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinLossTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinPartTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.set.SetAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferWholeTransaction;
import tech.ebp.oqm.core.api.model.units.UnitUtils;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.money.MonetaryAmount;
import javax.money.format.AmountFormatQuery;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.time.ZonedDateTime;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
/**
 * Validators TODO:
 *
 *  - MonetaryAmounts have same currency?
 *  - perUnit both null or not null
 */
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
