#!/usr/bin/env python

import os
import fnmatch
import shutil


if __name__ == '__main__':
	
	# build a list of files to convert
	bbfiles = []
	for root, dirnames, filenames in os.walk('.'):
		for filename in fnmatch.filter(filenames, '*.bb'):
			bbfiles.append((os.path.join(root, filename), (filename)))

	# output directory
	if os.path.exists('html'):
		shutil.rmtree('html')
	os.makedirs('html')

	# convert all bb files to html
	for current_bb in bbfiles:

		# read contents of file
		with open(current_bb[0], 'r') as in_file:
			data = in_file.read()
			in_file.close()

		# simple tags to convert
		simpletags = (
			('<', '&lt;'),
			('>', '&gt;'),
			('[b]', '<b>'),
			('[i]', '<i>'),
			('\n', '<br>'),
			('[/b]', '</b>'),
			('[/i]', '</i>'),
			('[center]', '<center>'),
			('[/center]', '</center>'),
			('[img]', '<img src="'),
			('[/img]', '">'),
			('[code]', '<font size="3" color="#000066" face="verdana"><pre>'),
			('[code lang=dbp]', '<font size="3" color="#000066" face="verdana"><pre>'),
			('[/code]', '</pre></font>'),
			('[/href]', '</a>'))

		# convert
		for tag in simpletags:
			data = data.replace(tag[0], tag[1])

		# HACK: assume any left over closing brackets are from urls
		data = data.replace('[href=', '<a href="')
		data = data.replace(']', '">')

		# output file
		with open('html/' + current_bb[1].replace('bb', 'html'), 'w') as out_file:
			out_file.write(data)
			out_file.close()

		print 'processed file ' + current_bb[0]
