class AcmeToggle extends HTMLElement {
  constructor() {
    super();
    this._checked = false;
    this._disabled = false;
  }

  get checked() {
    return this._checked;
  }

  set checked(value) {
    this._checked = Boolean(value);
  }

  get disabled() {
    return this._disabled;
  }

  set disabled(value) {
    this._disabled = Boolean(value);
  }

  connectedCallback() {
    this.addEventListener("click", this._onClick);
  }

  disconnectedCallback() {
    this.removeEventListener("click", this._onClick);
  }

  _onClick = () => {
    if (this.disabled) return;
    this.checked = !this.checked;
    this.dispatchEvent(new CustomEvent("toggle-change", {
      detail: { checked: this.checked },
      bubbles: true,
      composed: true
    }));
  };
}

customElements.define("acme-toggle", AcmeToggle);
