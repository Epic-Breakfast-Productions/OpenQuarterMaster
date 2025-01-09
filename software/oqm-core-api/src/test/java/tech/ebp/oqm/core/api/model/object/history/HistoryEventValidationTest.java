package tech.ebp.oqm.core.api.model.object.history;

//@Slf4j
//class HistoryEventValidationTest extends ObjectValidationTest<HistoryEvent> {
//
//	public static Stream<Arguments> getValid() {
//		return Stream.of(
//			Arguments.of(
//				HistoryEvent.builder().type(EventType.ADD).build()
//			)
//		);
//	}
//
//	public static Stream<Arguments> getInvalid() {
//		return Stream.of(
//			Arguments.of( //TODO:: generates duplicate validation errors
//				new HistoryEvent(),
//				new HashMap<String, String>() {{
//					put("type", "must not be null");
//				}}
//			)
//		);
//	}
//}