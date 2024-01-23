<?php

namespace Ebprod\OqmCoreDepot\context;

use Ebprod\OqmCoreDepot\markdown\Markdown;
use Ebprod\OqmCoreDepot\sanitizers\Sanitizer;

class RunByContext {
	private ?string $logo;
	private ?string $name;
	private ?string $email;
	private ?string $phone;
	private ?string $website;
	private string $motd = "";
	
	public function __construct() {
		$this->logo = Sanitizer::allHtml(ContextUtils::getEnv("CFG_RUNBY_LOGO"));
		$this->name = Sanitizer::allHtml(ContextUtils::getEnv("CFG_RUNBY_NAME"));
		$this->email = Sanitizer::allHtml(ContextUtils::getEnv("CFG_RUNBY_EMAIL"));
		$this->phone = Sanitizer::allHtml(ContextUtils::getEnv("CFG_RUNBY_PHONE"));
		$this->website = Sanitizer::allHtml(ContextUtils::getEnv("CFG_RUNBY_WEBSITE"));
		$this->motd = Markdown::parseMdFull(ContextUtils::getEnv("CFG_RUNBY_MOTD", ""));
	}
	
	public function getLogo(): ?string {
		return $this->logo;
	}
	
	public function hasLogo():bool{
		return !is_null($this->logo);
	}
	
	public function getName(): ?string {
		return $this->name;
	}
	public function hasName():bool{
		return !is_null($this->name);
	}
	
	public function getEmail(): ?string {
		return $this->email;
	}
	public function hasEmail():bool{
		return !is_null($this->email);
	}
	
	public function getPhone(): ?string {
		return $this->phone;
	}
	public function hasPhone():bool{
		return !is_null($this->phone);
	}
	
	public function getWebsite(): ?string {
		return $this->website;
	}
	public function hasWebsite():bool{
		return !is_null($this->website);
	}
	
	public function hasContact():bool{
		return $this->hasEmail() || $this->hasPhone() || $this->hasWebsite();
	}
	
	public function getMotd(): string {
		return $this->motd;
	}
}