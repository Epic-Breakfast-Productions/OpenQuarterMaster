package tech.ebp.oqm.plugin.mssController.model.module;

import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.response.ModuleInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OqmModuleInfo {

	/**
	 * The module info from the module itself.
	 */
	private ModuleInfo moduleInfo;
	/**
	 * Map of Oqm DB ID to the associated infos
	 */
	private Map<String, ModuleOqmDbInfo> associatedStorageBlockIds;


	public String getModuleSerialId(){
		return this.moduleInfo.getSerialId();
	}

	/**
	 * Gets the module oqm db info of the oqm db specified
	 * @param oqmDbId The id of the oqm db to get the info for
	 * @return Empty if no info for this database exists.
	 */
	public Optional<ModuleOqmDbInfo> getDbInfo(String oqmDbId) {
		ModuleOqmDbInfo info = this.associatedStorageBlockIds.get(oqmDbId);

		if (info == null) {
			return Optional.empty();
		}
		return Optional.of(info);
	}

	/**
	 * Gets the associated top level storage block id in the db given for this module.
	 * @param oqmDbId The id of the db to get the storage block from
	 * @return Empty if not present in the database, The id of the storage block otherwise.
	 */
	public Optional<String> getAssociatedStorageBlockId(String oqmDbId) {
		Optional<ModuleOqmDbInfo> info = this.getDbInfo(oqmDbId);

		if(info.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(info.get().getAssociatedStorageBlockId());
	}

	/**
	 *
	 * @param oqmDbId
	 * @param blockNum
	 * @return Empty if not present in the database, the storage block id for the specific block in the module otherwise.
	 */
	public Optional<String> getStorageBlockIdForBlock(String oqmDbId, int blockNum) {
		Optional<ModuleOqmDbInfo> info = this.getDbInfo(oqmDbId);

		if(info.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(info.get().getStorageBlockIdForBlock(blockNum));
	}

	/**
	 *
	 * @param oqmDbId
	 * @param storageBlockId
	 * @return Empty if not present in the database, the block number for the associated storage block otherwise.
	 */
	public Optional<Integer> getBlockNumForStorageBlockId(String oqmDbId, String storageBlockId){
		Optional<ModuleOqmDbInfo> info = this.getDbInfo(oqmDbId);

		if(info.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(info.get().getBlockNumForStorageBlockId(storageBlockId));
	}

}
