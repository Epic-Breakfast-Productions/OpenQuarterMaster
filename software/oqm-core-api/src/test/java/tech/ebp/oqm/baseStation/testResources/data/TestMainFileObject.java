package tech.ebp.oqm.baseStation.testResources.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class TestMainFileObject extends FileMainObject {
	
	@NotNull
	@NotEmpty
	private String testField;
	
	private int intValue = 0;
	private long longValue = 0;
	private double floatValue = 0;
//	private BigInteger bigIntValue = BigInteger.ZERO;
//	private BigDecimal bigDecimalValue = BigDecimal.ZERO;
	
	public TestMainFileObject(String testField){
		this.setTestField(testField);
	}
	
	public TestMainFileObject(String testField, int intValue){
		this.setTestField(testField);
		this.setIntValue(intValue);
	}
	public TestMainFileObject(String testField, long longValue){
		this.setTestField(testField);
		this.setLongValue(longValue);
	}
	public TestMainFileObject(String testField, double floatValue){
		this.setTestField(testField);
		this.setFloatValue(floatValue);
	}
	
//	public TestMainObject(String testField, BigInteger bigIntValue){
//		this.setTestField(testField);
//		this.setBigIntValue(bigIntValue);
//	}
//	public TestMainObject(String testField, BigDecimal bigDecimalValue){
//		this.setTestField(testField);
//		this.setBigDecimalValue(bigDecimalValue);
//	}
}
