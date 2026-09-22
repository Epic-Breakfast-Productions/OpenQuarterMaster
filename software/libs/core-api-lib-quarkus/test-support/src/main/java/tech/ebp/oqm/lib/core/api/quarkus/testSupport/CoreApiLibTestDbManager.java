/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package tech.ebp.oqm.lib.core.api.quarkus.testSupport;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.providers.base.Bool;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.models.ItemStorageType;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers.ImageHelper;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers.ItemHelper;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers.StorageHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.random.RandomGenerator;

import static io.restassured.RestAssured.given;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoreApiLibTestDbManager extends CoreApiLibClientHelper {


	public static void clearAllDbs(String auth){
		log.info("Clearing all databases on test instance of OQM core api. Auth: {}", auth);

//		getCoreApiClientService()
//			.manageDbClearAll(auth)
//			.await().indefinitely();

		given()
			.when()
			.header(new Header("Authorization", auth))
			.accept(ContentType.JSON)
			.delete(CoreApiLibTestUtils.getCoreApiBaseUri() + "/api/v1/inventory/manage/db/clearAllDbs")
			.then()
			.statusCode(200)
		;


		log.info("Done clearing all databases.");
	}

	private static <T> Optional<T> randFromList(List<T> list){
		if(list.isEmpty()){
			return Optional.empty();
		}

		return Optional.of(list.get(RandomGenerator.getDefault().nextInt(list.size())));
	}


	public static void populateDb(
		String auth,
		String oqmDbNameId,
		PopulateOptions options
	) throws IOException {
		List<String> images = new ArrayList<>(options.getNumImages());
		List<String> storageBlocks = new ArrayList<>(options.getNumBlocks());
		List<ObjectNode> items = new ArrayList<>(options.getNumBlocks());

		{//images
			for(int i = 0; i < options.getNumImages(); i++){
				images.add(ImageHelper.newImage(auth, oqmDbNameId).get("id").asText());
			}
		}

		{//storage blocks
			for(int i = 0; i < options.getNumBlocks(); i++){
				ObjectNode template = StorageHelper.getStorageBlockTemplate();

				if(options.getBlockImages()){
					template.putArray("imageIds").add(randFromList(images).get());
				}


				ObjectNode curBlock = StorageHelper.addStorageBlock(
					auth,
					oqmDbNameId,
					template
				);

				storageBlocks.add(curBlock.get("id").asText());
			}
		}
		{//inventory items
			for(int i = 0; i < options.getNumBlocks(); i++){
				ObjectNode template = ItemHelper.getItemTemplate(
					ItemStorageType.values()[i % ItemStorageType.values().length].name()
				);

				if(options.getItemImages()){
					template.putArray("imageIds").add(randFromList(images).get());
				}


				ObjectNode curItem = ItemHelper.addItem(
					auth,
					oqmDbNameId,
					template
				);

				items.add(curItem);
			}
		}
















	}


	@Data
	@Setter(AccessLevel.PRIVATE)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@Builder
	public static class PopulateOptions{

		@Builder.Default
		private Integer numImages = 50;

		@Builder.Default
		private Integer numBlocks = 50;

		@Builder.Default
		private Integer numItems = 50;

		@Builder.Default
		private Boolean blockImages = true;

		@Builder.Default
		private Boolean itemImages = true;

	}

}
