<img src="media/logo/Quarter%20Master%20Main%20Logo%20Outlined.svg" alt="Open QuarterMaster Logo">

# Open QuarterMaster

<!-- https://shields.io -->
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/Epic-Breakfast-Productions/OpenQuarterMaster)
![GitHub all releases](https://img.shields.io/github/downloads/Epic-Breakfast-Productions/OpenQuarterMaster/total)
![Core API](https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/actions/workflows/core-api.yml/badge.svg)

[//]: # (![Station Captain]&#40;https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/actions/workflows/stationCaptain.yml/badge.svg&#41;)
<a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/releases.atom">![Rss](https://img.shields.io/badge/rss-F88900?style=for-the-badge&logo=rss&logoColor=white)</a>
![GitHub](https://img.shields.io/github/license/Epic-Breakfast-Productions/OpenQuarterMaster)
[![](https://dcbadge.limes.pink/api/server/cpcVh6SyNn?style=flat)](https://discord.gg/cpcVh6SyNn)<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-16-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

**Inventory without a catch, and all the hooks**

Open Quartermaster is an open source inventory management system, designed to be simple to use yet powerful and extendable. The last inventory management system you will ever need!

We are very much in development still, so check back often! We are also accepting any and all assistance, so feel free to report issues or feature requests, as well as pull requests! Additionally, feel free to ask questions in the [Discussions](https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/discussions) or just hang out with us on our [Discord](https://discord.gg/cpcVh6SyNn)

## Quick Links

 - For a quick start running on your own computer, check out [Single Host Deployment](deployment/Single%20Host)
 - To see all the ways you can deploy OQM for yourself, see [Deployment](deployment/)
 - For information on the overall system, see the [software](software/) directory.

## How it works

How we accomplish the goal of being the only inventory management system you could ever need is through our modular design. The main component of Open QuarterMaster is the [Core API](software/core/oqm-core-api). Think of this as the central hub and core functionality of the system. It handles all the generic inventory management tasks; what is stored where, and facts about what is stored. This central component is designed to be, on the whole, generic and accessible. We also have a frontend for the core API called the [Base Station](software/core/oqm-core-base-station), which lets you have direct and easy to navigate access to your inventory.

To cover specific use-cases, we have what we call [Plugins](software/plugins). These are components that extend the functionality of the basic inventory management, and fill additional needs with their own capabilities. Examples could include Smart Refrigerator integrations, a system for interacting with physical storage mediums, Point of Sale Systems, Workflow management.. the list is endless. You could even create your own!

In the theme of flexibility, the system is designed to be run in many different environments. It is just as home on the cloud as well as something as small as a [Raspberry Pi](https://www.raspberrypi.com/). This is accomplished using containers, segmenting each software component, ensuring flexibility and ease of management.

To get started on your own hardware, please see [Single Host Deployment](deployment/Single%20Host)

For more information on the overall system, see the [software](software/) directory. 

## On Privacy

Being an open initiative, we take great care to ensure you are in control of your own data. None of the software we include here phones home at all, with the brief exception of Station Captain, which looks to this Git repository for installations and updates. If you have a simple setup on your own hardware, you can expect your data to stay with you, and not transmitted anywhere by the software we include here.

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="http://gjstewart.net"><img src="https://avatars.githubusercontent.com/u/7083701?v=4?s=100" width="100px;" alt="Greg Stewart"/><br /><sub><b>Greg Stewart</b></sub></a><br /><a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/issues?q=author%3AGregJohnStewart" title="Bug reports">🐛</a> <a href="#business-GregJohnStewart" title="Business development">💼</a> <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=GregJohnStewart" title="Code">💻</a> <a href="#content-GregJohnStewart" title="Content">🖋</a> <a href="#data-GregJohnStewart" title="Data">🔣</a> <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=GregJohnStewart" title="Documentation">📖</a> <a href="#design-GregJohnStewart" title="Design">🎨</a> <a href="#ideas-GregJohnStewart" title="Ideas, Planning, & Feedback">🤔</a> <a href="#infra-GregJohnStewart" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a> <a href="#maintenance-GregJohnStewart" title="Maintenance">🚧</a> <a href="#projectManagement-GregJohnStewart" title="Project Management">📆</a> <a href="#tool-GregJohnStewart" title="Tools">🔧</a> <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=GregJohnStewart" title="Tests">⚠️</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/anixon-rh"><img src="https://avatars.githubusercontent.com/u/55244503?v=4?s=100" width="100px;" alt="Anthony Nixon"/><br /><sub><b>Anthony Nixon</b></sub></a><br /><a href="#infra-anixon-rh" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a> <a href="#mentoring-anixon-rh" title="Mentoring">🧑‍🏫</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/kfrankli"><img src="https://avatars.githubusercontent.com/u/3671139?v=4?s=100" width="100px;" alt="kfrankli"/><br /><sub><b>kfrankli</b></sub></a><br /><a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=kfrankli" title="Documentation">📖</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/ajoline"><img src="https://avatars.githubusercontent.com/u/80230444?v=4?s=100" width="100px;" alt="ajoline"/><br /><sub><b>ajoline</b></sub></a><br /><a href="#mentoring-ajoline" title="Mentoring">🧑‍🏫</a> <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/pulls?q=is%3Apr+reviewed-by%3Aajoline" title="Reviewed Pull Requests">👀</a> <a href="#security-ajoline" title="Security">🛡️</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/piercde12"><img src="https://avatars.githubusercontent.com/u/132835358?v=4?s=100" width="100px;" alt="piercde12"/><br /><sub><b>piercde12</b></sub></a><br /><a href="#business-piercde12" title="Business development">💼</a> <a href="#content-piercde12" title="Content">🖋</a> <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=piercde12" title="Documentation">📖</a> <a href="#design-piercde12" title="Design">🎨</a> <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=piercde12" title="Tests">⚠️</a> <a href="#userTesting-piercde12" title="User Testing">📓</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/kyleclarktech"><img src="https://avatars.githubusercontent.com/u/86199883?v=4?s=100" width="100px;" alt="Kyle Clark"/><br /><sub><b>Kyle Clark</b></sub></a><br /><a href="#content-kyleclarktech" title="Content">🖋</a> <a href="#ideas-kyleclarktech" title="Ideas, Planning, & Feedback">🤔</a> <a href="#mentoring-kyleclarktech" title="Mentoring">🧑‍🏫</a> <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/pulls?q=is%3Apr+reviewed-by%3Akyleclarktech" title="Reviewed Pull Requests">👀</a> <a href="#security-kyleclarktech" title="Security">🛡️</a> <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=kyleclarktech" title="Tests">⚠️</a> <a href="#userTesting-kyleclarktech" title="User Testing">📓</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/DanielKrejska"><img src="https://avatars.githubusercontent.com/u/44409727?v=4?s=100" width="100px;" alt="DanielKrejska"/><br /><sub><b>DanielKrejska</b></sub></a><br /><a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=DanielKrejska" title="Code">💻</a></td>
    </tr>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/LouisBarbier"><img src="https://avatars.githubusercontent.com/u/116147989?v=4?s=100" width="100px;" alt="LouisBarbier"/><br /><sub><b>LouisBarbier</b></sub></a><br /><a href="#content-LouisBarbier" title="Content">🖋</a> <a href="#projectManagement-LouisBarbier" title="Project Management">📆</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/BrendanAndrews"><img src="https://avatars.githubusercontent.com/u/113378507?v=4?s=100" width="100px;" alt="Brendan Andrews"/><br /><sub><b>Brendan Andrews</b></sub></a><br /><a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=BrendanAndrews" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/Spitzer-Tech"><img src="https://avatars.githubusercontent.com/u/37207444?v=4?s=100" width="100px;" alt="Spitzer-Tech"/><br /><sub><b>Spitzer-Tech</b></sub></a><br /><a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/issues?q=author%3ASpitzer-Tech" title="Bug reports">🐛</a> <a href="#design-Spitzer-Tech" title="Design">🎨</a> <a href="#example-Spitzer-Tech" title="Examples">💡</a> <a href="#financial-Spitzer-Tech" title="Financial">💵</a> <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=Spitzer-Tech" title="Tests">⚠️</a> <a href="#userTesting-Spitzer-Tech" title="User Testing">📓</a> <a href="#ideas-Spitzer-Tech" title="Ideas, Planning, & Feedback">🤔</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/steve-dill"><img src="https://avatars.githubusercontent.com/u/175041555?v=4?s=100" width="100px;" alt="steve-dill"/><br /><sub><b>steve-dill</b></sub></a><br /><a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/issues?q=author%3Asteve-dill" title="Bug reports">🐛</a> <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=steve-dill" title="Code">💻</a> <a href="#ideas-steve-dill" title="Ideas, Planning, & Feedback">🤔</a> <a href="#infra-steve-dill" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a> <a href="#mentoring-steve-dill" title="Mentoring">🧑‍🏫</a> <a href="#tool-steve-dill" title="Tools">🔧</a> <a href="#userTesting-steve-dill" title="User Testing">📓</a> <a href="#maintenance-steve-dill" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/mulliar22"><img src="https://avatars.githubusercontent.com/u/168582625?v=4?s=100" width="100px;" alt="mulliar22"/><br /><sub><b>mulliar22</b></sub></a><br /><a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=mulliar22" title="Code">💻</a> <a href="#data-mulliar22" title="Data">🔣</a> <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=mulliar22" title="Documentation">📖</a> <a href="#example-mulliar22" title="Examples">💡</a> <a href="#plugin-mulliar22" title="Plugin/utility libraries">🔌</a> <a href="#research-mulliar22" title="Research">🔬</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/mallom21"><img src="https://avatars.githubusercontent.com/u/113387289?v=4?s=100" width="100px;" alt="mallom21"/><br /><sub><b>mallom21</b></sub></a><br /><a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=mallom21" title="Code">💻</a> <a href="#data-mallom21" title="Data">🔣</a> <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=mallom21" title="Documentation">📖</a> <a href="#example-mallom21" title="Examples">💡</a> <a href="#plugin-mallom21" title="Plugin/utility libraries">🔌</a> <a href="#research-mallom21" title="Research">🔬</a> <a href="#projectManagement-mallom21" title="Project Management">📆</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/Coletrane315"><img src="https://avatars.githubusercontent.com/u/52933325?v=4?s=100" width="100px;" alt="Coletrane315"/><br /><sub><b>Coletrane315</b></sub></a><br /><a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=Coletrane315" title="Code">💻</a> <a href="#data-Coletrane315" title="Data">🔣</a> <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=Coletrane315" title="Documentation">📖</a> <a href="#example-Coletrane315" title="Examples">💡</a> <a href="#plugin-Coletrane315" title="Plugin/utility libraries">🔌</a> <a href="#research-Coletrane315" title="Research">🔬</a></td>
    </tr>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/simonramsey0"><img src="https://avatars.githubusercontent.com/u/149826099?v=4?s=100" width="100px;" alt="Simon Ramsey"/><br /><sub><b>Simon Ramsey</b></sub></a><br /><a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=simonramsey0" title="Code">💻</a> <a href="#content-simonramsey0" title="Content">🖋</a> <a href="#plugin-simonramsey0" title="Plugin/utility libraries">🔌</a> <a href="#research-simonramsey0" title="Research">🔬</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/thederpylama"><img src="https://avatars.githubusercontent.com/u/35352055?v=4?s=100" width="100px;" alt="Ian Lauver"/><br /><sub><b>Ian Lauver</b></sub></a><br /><a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/commits?author=thederpylama" title="Code">💻</a> <a href="#content-thederpylama" title="Content">🖋</a> <a href="#plugin-thederpylama" title="Plugin/utility libraries">🔌</a> <a href="#research-thederpylama" title="Research">🔬</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
