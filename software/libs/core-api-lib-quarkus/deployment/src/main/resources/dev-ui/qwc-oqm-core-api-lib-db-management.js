import { LitElement, html, css} from 'lit';
import { JsonRpc } from 'jsonrpc';

// import { jokes } from 'build-time-data';

export class OqmCoreApiDbManagementComponent extends LitElement {
	jsonRpc = new JsonRpc(this);

	static styles = css`
	`;

	static properties = {
		_isStreaming: {state: true}
	};

	constructor() {
		super();
		this._isStreaming = false;
	}

	render() {
		return html`<h1>Manage OQM Core API Data</h1>
            <div class="buttonBar">
				<vaadin-button class="button" theme="danger" @click=${this._resetDb}>
					<vaadin-icon icon="font-awesome-solid:database"></vaadin-icon> Reset DB
				</vaadin-button>
				<vaadin-button class="button" theme="success" @click=${this._populateDb}>
					<vaadin-icon icon="font-awesome-solid:database"></vaadin-icon> Reset DB and Load Dataset
				</vaadin-button>
            </div>
		<div id="oqm-core-api-lib-db-management-operation-results">
			
		</div>
            `;
	}

	_resetDb(){
		console.log("Resetting DB.");
		this.jsonRpc.resetDb().then(response => {
			console.log("Completed resetting db: ", response);
			//TODO:: apparently not the way to do this
			// document.getElementById("oqm-core-api-lib-db-management-operation-results").innerHTML =+ "<br />"+response.result;
		});
	}

	_populateDb(){
		console.log("Populating DB.");
		this.jsonRpc.resetAndPopulateDb({db: "default"}).then(response => {
			console.log("Completed resetting db: ", response);
			//TODO:: apparently not the way to do this
			// document.getElementById("oqm-core-api-lib-db-management-operation-results").innerHTML =+ "<br />"+response.result;
		});
	}

}
customElements.define('qwc-oqm-core-api-lib-db-management', OqmCoreApiDbManagementComponent);