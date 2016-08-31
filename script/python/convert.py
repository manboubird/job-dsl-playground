#!/usr/bin/env python3

import sys
import pytoml as toml
from jinja2 import Template, Environment, FileSystemLoader
 
argvs = sys.argv
argc = len(argvs)
fname = ''
if argc > 1:
    fname = argvs[1]

with open(fname) as f:
    config = toml.loads(f.read())

template_filename = 'select.j2'
loader = FileSystemLoader(searchpath="./script/python/j2_templates", encoding='utf8')
env = Environment(loader = loader)
tpl = env.get_template(template_filename)

content = tpl.render({'dic': config['select_table']})

# print(toml.dumps(config), file=sys.stdout)
# print('---', file=sys.stdout)
print(content, file=sys.stdout)
