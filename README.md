# Dash docset generator for the Sublime Text Plugin API

<a href='https://ko-fi.com/X8X19BQH' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://az743702.vo.msecnd.net/cdn/kofi1.png?v=0' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

This script generates a [Dash](http://kapeli.com/dash) docset for Sublime
Text plugin development.

It contains an indexed reference of all available plugin classes, modules
and methods, as well as auto-generated compatibility info for Sublime Text
2 vs 3, because any ST plugin developer knows that the APIs have subtle yet
potentially show-stopping differences.

## Execution

Check out the repo, [install Leiningen](http://leiningen.org/#install),
then just `lein run` should be enough to generate the docset files.

The [online Sublime Text API reference](http://sublimetext.com/docs/3/api_reference.html)
MUST be available - the documentation is downloaded from there.

After this `Sublime_Text_Plugin_API.docset` is ready to be installed into
Dash.

## License

This script is released under the WTFPL license.

(c) 2015 [Leonid Shevtsov](http://leonid.shevtsov.me)
