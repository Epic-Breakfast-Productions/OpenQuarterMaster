package tech.ebp.oqm.plugin.mssController.module.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class HighlightBlocksCommand extends MssCommand{
	@Getter
	private static final HighlightBlocksCommand instance = new HighlightBlocksCommand();
	
	int duration = 30;
	boolean carry = false;
	boolean beep = true;
	private List<BlockHighlightSettings> storageBlocks = new ArrayList<>();
	
	public HighlightBlocksCommand(
		String color,
		Set<Integer> blockNumbers
	){
		this.storageBlocks.addAll(blockNumbers.stream().map((Integer curBlockNum) ->new BlockHighlightSettings(curBlockNum, color)).toList());
	}
	
	public HighlightBlocksCommand(
		boolean carry,
		String color,
		Set<Integer> blockNumbers
	){
		this(color, blockNumbers);
		this.carry = carry;
	}
	
	public HighlightBlocksCommand(
		boolean carry,
		String color,
		Integer... blockNumbers
	){
		this(color, Set.of(blockNumbers));
		this.carry = carry;
	}
	
	
	public int getNumSettings(){
		return this.getStorageBlocks().size();
	}
	
	@Override
	public CommandType getCommand() {
		return CommandType.HIGHLIGHT_BLOCKS;
	}
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class BlockHighlightSettings {
		private int blockNum = 0;
		private String powerState = "ON";
		private String color = "RAND";
		
		public BlockHighlightSettings(int blockNum){
			this.blockNum = blockNum;
		}
		public BlockHighlightSettings(int blockNum, String color){
			this(blockNum);
			this.color = color;
		}
	}
}
