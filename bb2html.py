#!/usr/bin/env python

import optparse


if __name__ == '__main__':

	def _format_option_string(option):
                ''' ('-o', '--option') -> -o, --format METAVAR'''

                opts = []

                if option._short_opts: opts.append(option._short_opts[0])
                if option._long_opts: opts.append(option._long_opts[0])
                if len(opts) > 1: opts.insert(1, ', ')

                if option.takes_value(): opts.append(' %s' % option.metavar)

                return "".join(opts)

	fmt = optparse.IndentedHelpFormatter(width=max_width, max_help_position=max_help_position)
        fmt.format_option_strings = _format_option_string

        kw = {
                'version'   : __version__,
                'formatter' : fmt,
                'usage' : '%prog [options] url [url...]',
                'conflict_handler' : 'resolve',
        }

        parser = optparse.OptionParser(**kw)

	general = optparseOptionGroup(parser, 'General Options')

	general.add_option('-h', '--help',
		action = 'help', help='print this help text and exit')

	parser.add_option_group(general)

