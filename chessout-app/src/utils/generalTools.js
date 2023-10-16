export function intlNumberFormat(number, locales, minDigits, maxDigits) {
	const options = {
		minimumFractionDigits: minDigits !== undefined ? minDigits : 2,
		maximumFractionDigits: maxDigits !== undefined ? maxDigits : 2,
	};

	return new Intl.NumberFormat(locales ? locales : "en-GB", options).format(number);
}

export const openInNewTab = (url) => {
	const newWindow = window.open(url, '_blank', 'noopener,noreferrer')
	if (newWindow) newWindow.opener = null;
};

export const openInSameTab = (url) => {
	const newWindow = window.open(url, '_self', 'noopener,noreferrer')
	if (newWindow) newWindow.opener = null;
};