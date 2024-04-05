/*
 * Units of Measurement Jackson Library
 * Copyright (c) 2005-2021, Werner Keil and others.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *    and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of JSR-385, Indriya nor the names of their contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package tech.ebp.oqm.core.api.model.jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Quantity.Scale;
import javax.measure.Unit;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author bantu
 */
@SuppressWarnings("rawtypes")
@Deprecated
public class QuantityJsonDeserializer extends StdDeserializer<Quantity> {
	
	public QuantityJsonDeserializer() {
		super(Quantity.class);
	}
	
	@Override
	public Quantity deserialize(JsonParser jp, DeserializationContext deserializationContext)
		throws IOException, JsonProcessingException {
		TreeNode root = jp.readValueAsTree();
		if (root.get("value") == null) {
			throw new JsonParseException(jp, "Value not found for quantity type.");
		}
		if (root.get("unit") == null) {
			throw new JsonParseException(jp, "Unit not found for quantity type.");
		}
		if (root.get("scale") == null) {
			throw new JsonParseException(jp, "Scale not found for quantity type.");
		}
		
		ObjectCodec codec = jp.getCodec();
		BigDecimal value = codec.treeToValue(root.get("value"), BigDecimal.class);
		Unit<?> unit = codec.treeToValue(root.get("unit"), Unit.class);
		Scale scale = Scale.valueOf(codec.treeToValue(root.get("scale"), String.class));
		
		return Quantities.getQuantity(value, unit, scale);
	}
}
