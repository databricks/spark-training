
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


$(document).ready(function() {
	$('#toc').toc({exclude: ''});
	styleCode();
});
