import { LitElement, html, css} from 'lit';
import { jokes } from 'build-time-data';

export class QwcJokesList extends LitElement {

	static styles = css` 
        .buttonBar {
            display: flex;
            justify-content: space-between;
            gap: 10px;
            align-items: center;
            width: 90%;
            color: var(--lumo-primary-text-color); 
        }

        .buttonBar .button {
            width: 100%;
        }
        `;

	static properties = {
		_jokes: {state: true},
		_numberOfJokes: {state: true},
		_message: {state: true},
		_isStreaming: {state: true}
	};

	constructor() {
		super();
		this._jokes = [];
		this._numberOfJokes = 0;
		this._isStreaming = false;
	}

	connectedCallback() {
		super.connectedCallback();
		jokes.forEach((joke) =>{
			var item = this._toJokeItem(joke);
			this._jokes.push(item);
		});
		this._numberOfJokes = this._jokes.length;
	}

	disconnectedCallback() {
		if(this._isStreaming){
			this._observer.cancel();
		}
		super.disconnectedCallback()
	}

	render() {
		return html`<h3>Here are ${this._numberOfJokes} jokes</h3> 
            <vaadin-message-list .items="${this._jokes}"></vaadin-message-list>

            ${this._renderLoadingMessage()}
            <div class="buttonBar">
                <vaadin-button class="button" theme="success" @click=${() => this._fetchMoreJokes()}>
                    <vaadin-icon icon="font-awesome-solid:comment"></vaadin-icon> Tell me more jokes
                </vaadin-button>
            </div>
            `;
	}

	// ... more private methods
}
customElements.define('qwc-core-api-lib-quarkus', QwcJokesList);