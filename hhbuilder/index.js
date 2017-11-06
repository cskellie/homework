document.addEventListener("DOMContentLoaded", function() {
	init();
}, false);

/**
 * Initializes the changes necessary for the DOM after its already been rendered.
 */
function init() {
	// Tag the form with an id
	document.getElementsByTagName('form')[0].setAttribute('id', 'form');
	
	// Modify the ADD button to type it and add functionality
	var addBtn = document.getElementsByClassName('add')[0];
	addBtn.type = 'button';
	addBtn.onclick = function() {
		if (validateForm()) {
			addRow();
			clearForm();
		}
	};
	
	// Modify the SUBMIT button to add functionality
	var btns = document.getElementsByTagName('button');
	for (var i=0; i<btns.length; i++) {
		var type = btns[i].type;
		if (type === 'submit') {
			btns[i].onclick = function() {
				var bool =  submitHousehold();
				clearForm();
				return bool;
			}
		}
	}
	
	// Add a table to the page
	addHeader();
}

/**
 * Adds a table with headers to the page.
 * @returns the new table
 */
function addHeader() {
	var builderDiv = document.getElementsByClassName('builder')[0];
	var houseDiv = document.createElement('table');
	houseDiv.setAttribute('id', 'house');
	houseDiv.className = 'house';
	
	// Get the label text directly from the DOM
	var labels = document.getElementsByTagName('label');
	for (var i=0; i<labels.length; i++) {
		houseDiv.appendChild(createHeaderColumn(labels[i]));
	}
	// Add an invisible column for the REMOVE button
	var removeTh = document.createElement('th');
	houseDiv.appendChild(removeTh);
	
	builderDiv.appendChild(houseDiv);
	return houseDiv;
}

/**
 * Creates a header column from the given label element.
 * @param label a label element
 * @returns the new header column
 */
function createHeaderColumn(label) {
	var th = document.createElement('th');
	// Using .innerText because .innerHTML gives the input as well
	th.innerHTML = label.innerText;
	return th;
}

/**
 * Creates a row containing the form information.
 */
function addRow() {
	var houseDiv = document.getElementById('house');
	var tr = document.createElement('tr');
	
	var labels = document.getElementsByTagName('label');
	for (var i=0; i<labels.length; i++) {
		tr.appendChild(createCellInfo(labels[i].children[0]));
	}
	var removeTd = document.createElement('td');
	removeTd.appendChild(createRemoveBtn());
	removeTd.headers = 'remove';
	tr.appendChild(removeTd);
	
	houseDiv.appendChild(tr);
}

/**
 * Creates an individual cell from a form input.
 * @param input the input element to create the cell from
 * @returns the new cell
 */
function createCellInfo(input) {
	var td = document.createElement('td');
	td.innerHTML = input.type === 'checkbox' ? input.checked : input.value;
	td.headers = input.name;
	return td;
}

/**
 * Validates the input on the form. Age and relationship are required; age > 0.
 * @returns true if the form passes all validations, else false
 */
function validateForm() {
	var message = '';
	
    var age = document.forms['form']['age'].value;
    var relationship = document.forms['form']['rel'].value;
    if (age === '') {
        message +='An age must be provided.\n';
    }
    if (age < 0) {
    	message += 'Age must be greater than 0.\n';
    }
    if (relationship === '') {
    	message += 'A relationship must be selected.';
    }
    // Gather all errors, and send back
    if (message.length > 0) {
    	alert(message);
    	return false;
    }
    
    return true;
}

/**
 * Creates a button with remove functionality. Removes the corresponding row from 
 * a table.
 * @returns the new button
 */
function createRemoveBtn() {
	var removeBtn = document.createElement('button');
	removeBtn.innerHTML = 'Remove';
	removeBtn.type = 'button';
	removeBtn.className = 'remove';
	removeBtn.onclick = function() {
		var row = this.parentNode.parentNode;
		row.parentNode.removeChild(row);
	};
	return removeBtn;
}

/**
 * Clears the form
 */
function clearForm() {
	document.getElementsByTagName('form')[0].reset();
}

/**
 * Submits the table to the server. Gathers all the table data into JSON, and stringifies 
 * it for the 'request' to the server (really we're setting it to the debug element
 * and showing the element).
 * @returns false so the page does not refresh
 */
function submitHousehold() {
	var finalArray = [];
	
	// Get all the rows in the table
	var rows = document.getElementById('house').rows;
	
	for (var i=0; i<rows.length; i++) {
		var row = rows[i];
		var cells = row.getElementsByTagName('td');
		var member = {};
		// For each cell, add the attribute with the value to the member object
		for (var j=0; j<cells.length-1; j++) {
			var cell = cells[j];
			var header = cell.headers;
			member[header] = cell.innerHTML;
		}
		// Add the member to the final array
		finalArray.push(member);
	}
	
	// stringify ALL THE THINGS!
	var jsonString = JSON.stringify(finalArray);
	var debug = document.getElementsByClassName('debug')[0];
	debug.innerHTML = jsonString;
	debug.style.display = 'inline-block';
	debug.style.whiteSpace = 'pre-wrap';

	return false;
}






