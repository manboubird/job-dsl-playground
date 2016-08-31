(function(window, $, _,CodeMirror){

    var App = {

        localStorageKey: 'dsl.lastScript',

        start: function(data) {
            this.data = data || {};
            var that = this;
            this.initLayout();
            this.initEditors();
            this.layout.resizeAll();

            var script = localStorage.getItem(this.localStorageKey);
            if (this.data.input !== undefined) {
                this.inputEditor.setValue(this.data.input || '');
                that.execute();
            } else if (script) {
                this.inputEditor.setValue(script);
            }

            $('.input .title .controls .execute').click(function(event) {
                event.preventDefault();
                that.execute();
            });

            $('.input .title .controls .save').click(function(event) {
                event.preventDefault();
                that.save();
            });

            this.showXmlEditor('DSL Output', '');

            $('body').css('visibility', 'visible');
        },

        showXmlEditor: function(title, output) {
            $('.output .title span').html(title);
            $('.output .title a.close-it').show();
            $('.code-wrapper').show();
            this.outputEditor.setValue(output);
            this.layout.resizeAll();
            this.outputEditor.refresh();
        },

        initLayout: function() {
            var that = this;
            this.layout = $('body').layout({
                north__paneSelector: '.header',
                north__resizable: false,
                north__spacing_open: 0,
                south__paneSelector: '.footer',
                south__resizable: false,
                south__spacing_open: 0,
                center__paneSelector: '.main'
            });
            $('body .main').layout({
                center__paneSelector: '.output',
                center__contentSelector: '.content',
                west__paneSelector: '.input',
                west__contentSelector: '.content',
                west__size: '40%',
                west__resizerCursor: 'ew-resize',
                resizable: true,
                findNestedContent: true,
                fxName: '',
                spacing_open: 3,
                spacing_closed: 3,
                slidable: false,
                closable: false,
                onresize_end: function (name, $el, state, opts) {
                    that.inputEditor.refresh();
                    that.outputEditor.refresh();
                }
            });
        },

        initEditors: function() {
            this.inputEditor = CodeMirror.fromTextArea($('.input textarea')[0], {
                matchBrackets: true,
                mode: 'toml',
                // mode: {name: "javascript", json: true},
                lineNumbers: true,
                tabSize: 4,
                indentUnit: 4,
                indentWithTabs: false,
                theme: 'pastel-on-dark',
                extraKeys: {
                    'Ctrl-Enter': _.bind(this.execute, this),
                    'Cmd-Enter': _.bind(this.execute, this)
                }
            });
            this.outputEditor = CodeMirror.fromTextArea($('.output textarea')[0], {
                matchBrackets: true,
                mode: 'sql',
                lineNumbers: true,
                foldGutter: true,
                gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter'],
                theme: 'pastel-on-dark'
            });
        },

        execute: function() {
            var script = this.inputEditor.getValue();
            localStorage.setItem(this.localStorageKey, script);
            $.ajax({
                url: '/execute',
                type: 'POST',
                dataType: 'json',
                data: {
                    script: script
                }
            }).done(_.bind(this.handleExecuteResponse, this));
            $('.input .loading').fadeIn(100)
        },

        save: function() {
            $('.modal').removeClass('hide').modal({});
        },

        handleExecuteResponse: function(resp) {
            $('.input .loading').fadeOut(100);
            var title, output;
            if (resp.stacktrace) {
                title = 'Error';
                output = resp.stacktrace;
            } else {
                title = 'DSL Output';
                output = _.map(resp.results, function(it, idx) {
                    var name = it.name || '[no name]';
                    return '-- ' + (idx + 1) + '. ' + name + '\n' + it.content;
                }).join('\n');
            }

            this.showXmlEditor(title, output);
        }
    };

    window.App = App;

})(window, jQuery, _, CodeMirror);
