async function searchUniversity(name) {
	let html = '';
	if (name.length > 2) {
		document.getElementById('results').innerHTML = "Loading...";
		const response = await fetch(`/api/universities/${name}`);
		const body = await response.json();
		if (body.length > 0) {
			html = `<table>`
			html += `<tr>
							<td><b>Name</b></td>
							<td><b>Country</b></td>
								<td><b>Website</b></td>
						</tr>`;
			for (university of body) {
				html += `<tr>
							<td>${university.name}</td>
							<td>${university.country}</td>
							<td><a href="${university.webPage}" target="_blank">${university.name}</a></td>
						</tr>`;
			}
			html += '</table>';
		} else {
			html = 'No results found.';
		}
	}
	document.getElementById('results').innerHTML = html;
}