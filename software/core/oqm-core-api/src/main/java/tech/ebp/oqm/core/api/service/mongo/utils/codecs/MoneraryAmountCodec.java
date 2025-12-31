package tech.ebp.oqm.core.api.service.mongo.utils.codecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.inject.Singleton;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;

import javax.measure.Quantity;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryAmountFactory;
import javax.money.NumberValue;
import java.text.NumberFormat;
import java.text.ParseException;


@Singleton
public class MoneraryAmountCodec implements Codec<MonetaryAmount> {
	private static NumberFormat NUMBER_FORMATTER = NumberFormat.getInstance();
	
	@Override
	public MonetaryAmount decode(BsonReader bsonReader, DecoderContext decoderContext) {
		MonetaryAmountFactory<?> amountFactory = Monetary.getDefaultAmountFactory();
		
		bsonReader.readStartDocument();
		while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			String fieldName = bsonReader.readName();
			if (fieldName.equals("valueStr")) {
				String numberStr = bsonReader.readString();
				try {
					amountFactory.setNumber(NUMBER_FORMATTER.parse(numberStr));
				} catch(ParseException e) {
					throw new RuntimeException("Failed to parse number: " + numberStr, e);
				}
			} else if (fieldName.equals("currency")) {
				amountFactory.setCurrency(bsonReader.readString());
			} if (fieldName.equals("valueDouble")) {
				bsonReader.readDouble();//needed to clear object
			}
		}
		bsonReader.readEndDocument();
		
		return amountFactory.create();
	}
	
	@Override
	public void encode(BsonWriter bsonWriter, MonetaryAmount amount, EncoderContext encoderContext) {
		bsonWriter.writeStartDocument();
		bsonWriter.writeDouble("valueDouble", amount.getNumber().doubleValue());
		bsonWriter.writeString("valueStr", NUMBER_FORMATTER.format(amount.getNumber()));
		bsonWriter.writeString("currency", amount.getCurrency().getCurrencyCode());
		bsonWriter.writeEndDocument();
	}
	
	@Override
	public Class<MonetaryAmount> getEncoderClass() {
		return MonetaryAmount.class;
	}
}
