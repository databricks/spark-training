
// From docs.scala-lang.org
function styleCode()
	{
		if (typeof disableStyleCode != "undefined")
		{
				return;
		}
		var a = false;
		$("pre code").parent().each(function()
		{
				if (!$(this).hasClass("prettyprint"))
				{
						$(this).addClass("prettyprint lang-scala linenums");
						a = true
				}
		});
		if (a) { prettyPrint() }
}

function codeTabs() {
	var counter = 0
	$("div.codetabs").each(function() {
		$(this).addClass("tab-content");

		// Insert the tab bar
		var tabBar = $('<ul class="nav nav-tabs" data-tabs="tabs"></ul>');
		$(this).before(tabBar);

		// Add each code sample to the tab bar:
		var codeSamples = $(this).children("div");
		codeSamples.each(function() {
			$(this).addClass("tab-pane");
			var lang = $(this).data("lang");
			lang = lang.substr(0, 1).toUpperCase() + lang.substr(1); // capitalize
			var id = "tab_" + lang + "_" + counter;
			$(this).attr("id", id);
			tabBar.append(
				'<li>' +
				'<a data-toggle="tab" href="#' + id + '">' +
				lang +
				'</a></li>'
			);
		});

		codeSamples.first().addClass("active");
		tabBar.children("li").first().addClass("active");
		counter++;
	});
}


$(document).ready(function() {
	codeTabs();
	$('#toc').toc({exclude: ''});
	styleCode();
});
