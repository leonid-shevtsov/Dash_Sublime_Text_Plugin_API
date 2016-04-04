build:
	rm -rf Sublime_Text_Plugin_API.old.docset
	cp -r Sublime_Text_Plugin_API.docset/ Sublime_Text_Plugin_API.old.docset
	lein run

diff:
	ksdiff Sublime_Text_Plugin_API.old.docset Sublime_Text_Plugin_API.docset

release:
	tar --exclude='.DS_Store' -cvzf Sublime_Text_Plugin_API.tgz Sublime_Text_Plugin_API.docset
