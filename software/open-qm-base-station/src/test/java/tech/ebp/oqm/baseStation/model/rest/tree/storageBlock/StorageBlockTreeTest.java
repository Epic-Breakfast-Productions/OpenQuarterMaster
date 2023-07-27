package tech.ebp.oqm.baseStation.model.rest.tree.storageBlock;

import tech.ebp.oqm.baseStation.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.baseStation.model.rest.tree.ParentedMainObjectTreeNode;
import tech.ebp.oqm.baseStation.model.testUtils.BasicTest;
import org.assertj.core.api.Fail;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TODO:: bulk stress test
 */
class StorageBlockTreeTest extends BasicTest {
	
	/*
	 *     1       2
	 *    /  \      \
	 *   3    4      5
	 *              /
	 *             6
	 *
	 *   ?
	 *    \
	 *     7
	 */
	private static final ObjectId id1 = ObjectId.get();
	private static final ObjectId id2 = ObjectId.get();
	private static final ObjectId id3 = ObjectId.get();
	private static final ObjectId id4 = ObjectId.get();
	private static final ObjectId id5 = ObjectId.get();
	private static final ObjectId id6 = ObjectId.get();
	private static final ObjectId id7 = ObjectId.get();
	
	private static final StorageBlock block1 = (StorageBlock) new StorageBlock().setId(id1);
	private static final StorageBlock block2 = (StorageBlock) new StorageBlock().setId(id2);
	private static final StorageBlock block3 = ((StorageBlock) new StorageBlock().setId(id3)).setParent(id1);
	private static final StorageBlock block4 = ((StorageBlock) new StorageBlock().setId(id4)).setParent(id1);
	private static final StorageBlock block5 = ((StorageBlock) new StorageBlock().setId(id5)).setParent(id2);
	private static final StorageBlock block6 = ((StorageBlock) new StorageBlock().setId(id6)).setParent(id5);
	private static final StorageBlock block7 = ((StorageBlock) new StorageBlock().setId(id7)).setParent(ObjectId.get());
	
	public static Stream<Arguments> getStorageBlocksNoOrphans() {
		return Stream.of(
			Arguments.of(List.of(block1, block2, block3, block4, block5, block6)),
			Arguments.of(List.of(block1, block1, block2, block3, block4, block5, block6)),
			Arguments.of(List.of(block6, block5, block4, block3, block2, block1))
		);
	}
	
	
	@ParameterizedTest
	@MethodSource("getStorageBlocksNoOrphans")
	public void testStorageBlockTreeNoOrphans(List<StorageBlock> storageBlocks) {
		StorageBlockTree tree = new StorageBlockTree();
		
		tree.add(storageBlocks.toArray(new StorageBlock[]{}));
		
		assertFalse(tree.hasOrphans());
		
		assertEquals(6, tree.getNodeMap().size());
		assertEquals(2, tree.getRootNodes().size());
		
		for (StorageBlockTreeNode tn : tree.getRootNodes()) {
			if (tn.getObjectId().equals(id1)) {
				assertEquals(2, tn.getChildren().size());
				for (ParentedMainObjectTreeNode<StorageBlock> tn2 : tn.getChildren()) {
					assertTrue(tn2.getObjectId().equals(id3) || tn2.getObjectId().equals(id4), "Child id of block 1 not what was expected");
					assertFalse(tn2.hasChildren());
				}
			} else if (tn.getObjectId().equals(id2)) {
				assertEquals(1, tn.getChildren().size());
				
				StorageBlockTreeNode tn2 = (StorageBlockTreeNode) tn.getChildren().toArray()[0];
				assertEquals(1, tn2.getChildren().size());
				assertTrue(tn2.hasChildren());
				
				StorageBlockTreeNode tn3 = (StorageBlockTreeNode) tn2.getChildren().toArray()[0];
				assertEquals(0, tn3.getChildren().size());
				assertFalse(tn3.hasChildren());
			} else {
				Fail.fail("One of the root nodes was not expected");
			}
		}
	}
	
	@Test
	public void testStorageBlockTreeOrphans() {
		StorageBlockTree tree = new StorageBlockTree();
		
		tree.add(block7);
		
		assertTrue(tree.hasOrphans());
	}
	
	@Test
	public void testCleanupStorageBlockTreeNode() {
		StorageBlockTree tree = new StorageBlockTree();
		tree.add(block1, block2, block3, block4, block5, block6);
		
		tree.cleanupTreeNodes(List.of(id5));
		
		assertEquals(1, tree.getRootNodes().size());
		
		StorageBlockTreeNode cur = tree.getRootNodes().stream().findFirst().get();
		assertEquals(id2, cur.getObjectId());
		
		assertEquals(1, cur.getChildren().size());
		cur = (StorageBlockTreeNode) cur.getChildren().stream().findFirst().get();
		
		assertEquals(id5, cur.getObjectId());
		
		assertEquals(1, cur.getChildren().size());
		cur = (StorageBlockTreeNode) cur.getChildren().stream().findFirst().get();
		
		assertEquals(id6, cur.getObjectId());
		
		assertEquals(0, cur.getChildren().size());
	}
	
}