<?php

namespace Ebprod\OqmCoreDepot\pageBuilders\icons;

class Icon {
	//pages
	public static Icon $overviewPage;
	public static Icon $categoriesPage;
	public static Icon $inventoryAdminPage;
	public static Icon $fileAttachmentsPage;
	public static Icon $imagesPage;
	public static Icon $itemsPage;
	public static Icon $itemListsPage;
	public static Icon $storageBlocksPage;
	
	//generic hinting
	public static Icon $add;
	public static Icon $subtract;
	public static Icon $addSubTrans;
	public static Icon $edit;
	public static Icon $remove;
	public static Icon $view;
	
	//Item Categories
	public static Icon $itemCategories;
	public static Icon $itemCategory;
	
	//checkout
	public static Icon $checkIn;
	public static Icon $checkInOut;
	public static Icon $checkOut;
	
	//file attachments
	public static Icon $fileAttachments;
	public static Icon $fileAttachment;
	
	//images
	public static Icon $image;
	public static Icon $images;
	
	//items
	public static Icon $item;
	public static Icon $items;
	
	//item lists
	public static Icon $itemLists;
	
	// storage block
	public static Icon $storageBlock;
	public static Icon $storageBlocks;
	
	//expiry and low stock
	public static Icon $expiring;
	public static Icon $expired;
	public static Icon $lowStock;
	
	// Interacting Entities
	public static Icon $externalServiceEntity;
	public static Icon $userEntity;
	public static Icon $baseStationEntity;
	
	// file types (not from mimetype)
	public static Icon $fileCsv;
	public static Icon $fileJson;
	
	//status
	public static Icon $statusDown;
	public static Icon $statusPending;
	public static Icon $statusUp;
	
	//theme
	public static Icon $themeAuto;
	public static Icon $themeDark;
	public static Icon $themeLight;
	
	
	//iconography
	public static Icon $barcode;
	public static Icon $copy;
	public static Icon $copyChecked;
	public static Icon $download;
	public static Icon $help;
	public static Icon $history;
	public static Icon $info;
	public static Icon $locked;
	public static Icon $newTab;
	public static Icon $print;
	public static Icon $qr;
	public static Icon $review;
	public static Icon $search;
	public static Icon $select;
	public static Icon $unlocked;
	public static Icon $you;
	
	public static function __constructStatic(): void {
		//core things
		Icon::$overviewPage = new Icon("speedometer2");
		Icon::$inventoryAdminPage = new Icon("database-gear");
		Icon::$add = new Icon("plus-circle");
		Icon::$subtract = new Icon("minus-circle");
		Icon::$addSubTrans = new Icon("plus-slash-minus");
		Icon::$edit = new Icon("pencil-fill");
		Icon::$remove = new Icon("trash-fill");
		Icon::$view = new Icon("eye");
		Icon::$itemCategories = new Icon("columns-gap");
		Icon::$itemCategory = Icon::$itemCategories;
		Icon::$checkIn = new Icon("box-arrow-in-down-right");
		Icon::$checkInOut = new Icon("arrow-down-up");
		Icon::$checkOut = new Icon("box-arrow-down-right");
		Icon::$fileAttachments = new Icon("file-earmark-medical");
		Icon::$fileAttachment = Icon::$fileAttachments;
		Icon::$image = new Icon("card-image");
		Icon::$images = new Icon("images");
		Icon::$item = new Icon("tag");
		Icon::$items = new Icon("tags");
		Icon::$itemLists = new Icon("list-task");
		Icon::$storageBlock = new Icon("box");
		Icon::$storageBlocks = new Icon("boxes");
		Icon::$expiring = new Icon("hourglass-split");
		Icon::$expired = Icon::$expiring;
		Icon::$lowStock = new Icon("graph-down-arrow");
		Icon::$externalServiceEntity = new Icon("bi-hdd-network-fill");
		Icon::$userEntity = new Icon("person-circle");
		Icon::$baseStationEntity = new Icon("hdd-rack");
		Icon::$fileCsv = new Icon("filetype-csv");
		Icon::$fileJson = new Icon("filetype-json");
		Icon::$statusDown = new Icon("lightning-charge-fill");
		Icon::$statusPending = new Icon("arrow-clockwise");
		Icon::$statusUp = new Icon("check-circle");
		Icon::$themeAuto = new Icon("circle-half");
		Icon::$themeDark = new Icon("moon-stars-fill");
		Icon::$themeLight = new Icon("brightness-high-fill");
		Icon::$barcode = new Icon("upc-scan");
		Icon::$copy = new Icon("clipboard-fill");
		Icon::$copyChecked = new Icon("clipboard-check-fill");
		Icon::$download = new Icon("download");
		Icon::$help = new Icon("question-circle");
		Icon::$history = new Icon("clock-history");
		Icon::$info = new Icon("info");
		Icon::$locked = new Icon("lock-fill");
		Icon::$newTab = new Icon("box-arrow-up-right");
		Icon::$print = new Icon("printer");
		Icon::$qr = new Icon("qr-code-scan");
		Icon::$review = new Icon("eyeglasses");
		Icon::$search = new Icon("search");
		Icon::$select = new Icon("check2-square");
		Icon::$unlocked = new Icon("unlock-fill");
		Icon::$you = new Icon("person-square");
		
		//derived
		Icon::$categoriesPage = Icon::$itemCategories;
		Icon::$fileAttachmentsPage = Icon::$fileAttachments;
		Icon::$imagesPage = Icon::$images;
		Icon::$itemsPage = Icon::$items;
		Icon::$itemListsPage = Icon::$itemLists;
		Icon::$storageBlocksPage = Icon::$storageBlocks;
		
	}
	
	//Iconography
	
	public static function getFileTypeIcon(string $mimeType): Icon {
		$typeParts = explode("/", $mimeType);
		$type = $typeParts[0];
		$subType = $typeParts[1];
		$subTypeUc = ucfirst($typeParts[1]);
		
		$ref = "file-earmark";
		$altText = $mimeType;
		switch ($type) {
			case "audio":
				$ref = "file-earmark-music";
				$altText = $subTypeUc . " audio";
				break;
			case "image":
				$ref = "file-earmark-image";
				$altText = $subTypeUc . " image";
				break;
			case "video":
				$ref = "file-earmark-play";
				$altText = $subTypeUc . " video";
				break;
			case "font":
				$ref = "file-earmark-font";
				$altText = $subTypeUc . " font";
				break;
			case "text":
				switch ($subType) {
					case "plain":
						$ref = "file-earmark-text";
						$altText = "Plain text";
						break;
					case "x-web-markdown":
						$ref = "filetype-md";
						$altText = "Markdown";
						break;
					case "csv":
						$ref = "filetype-csv";
						$altText = "CSV";
						break;
					
				}
				break;
			case "application":
				switch ($subType) {
					case "pdf":
						$ref = "file-earmark-pdf";
						$altText = "PDF";
						break;
					case "msword":
					case "vnd.openxmlformats-officedocument.wordprocessingml.document":
					case "vnd.oasis.opendocument.text":
					case "rtf":
						$ref = "file-earmark-richtext";
						$altText = match ($subType) {
							"msword" | "vnd.openxmlformats-officedocument.wordprocessingml.document" => "Microsoft Word",
							"rtf" => "Rich Text Document",
							"vnd.oasis.opendocument.text" => "Open Document Text"
						};
						break;
					case "vnd.ms-powerpoint":
					case "vnd.openxmlformats-officedocument.presentationml.presentation":
					case "vnd.oasis.opendocument.presentation":
						$ref = "file-earmark-slides";
						$altText = match ($subType) {
							"vnd.ms-powerpoint" | "vnd.openxmlformats-officedocument.presentationml.presentation" => "Microsoft PowerPoint",
							"vnd.oasis.opendocument.presentation" => "Open Document Presentation"
						};
						break;
					case "vnd.ms-excel":
					case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
					case "vnd.oasis.opendocument.spreadsheet":
						$ref = "file-earmark-spreadsheet";
						$altText = match ($subType) {
							"vnd.ms-excel" | "vnd.openxmlformats-officedocument.presentationml.sheet" => "Microsoft Excel",
							"vnd.oasis.opendocument.text" => "Open Document Spreadsheet"
						};
						break;
					case "json":
						$ref = "filetype-json";
						$altText = "JSON";
						break;
					case "octet-stream":
						$ref = "file-earmark-binary";
						$altText = "Binary Data";
						break;
					case "x-bzip":
					case "x-bzip2":
					case "gzip":
					case "vnd.rar":
					case "x-tar":
					case "zip":
					case "x-7z-compressed":
						$ref = "file-earmark-zip";
						$altText = $subTypeUc . " File Archive";
						break;
				}
				break;
		}
		
		return new Icon($ref, altText: $altText);
	}
	
	private IconType $type;
	private ?string $altText;
	private string $reference;
	private ?string $secondary;
	
	/**
	 * @param IconType $type
	 * @param string   $reference
	 */
	public function __construct(
		string   $reference,
		?string  $altText = null,
		?string  $secondary = null,
		IconType $type = IconType::BootstrapIcons
	) {
		$this->type = $type;
		$this->reference = $reference;
		$this->secondary = $secondary;
		$this->altText = $altText;
	}
	
	public function getType(): IconType {
		return $this->type;
	}
	
	public function getReference(): string {
		return $this->reference;
	}
	
	public function getSecondary(): string {
		return $this->secondary;
	}
	
	public function hasSecondary(): bool {
		return !is_null($this->secondary);
	}
	
	public function getAltText(): string {
		return $this->altText;
	}
	
	public function hasAltText(): bool {
		return !is_null($this->altText);
	}
}

Icon::__constructStatic();
