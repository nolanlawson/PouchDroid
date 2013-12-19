module.exports = function (grunt) {

    'use strict';

    grunt.initConfig({

        pkg: grunt.file.readJSON('package.json'),

        // Define our source and build folders
        js_src_path: 'src',
        js_lib_path: 'lib',
        js_build_path: '../library/res/raw',

        // Grunt Tasks
        concat: {
            options: {
                separator: ';'
            },
            js: {
                src: ['<%= js_src_path %>/*.js'],
                dest: '<%= js_build_path %>/<%= pkg.name %>.js'
            }
        },
        uglify: {
            options: {
                mangle: true
            },
            js: {
                src: '<%= concat.js.dest %>',
                dest: '<%= js_build_path %>/<%= pkg.name %>_min.js'
            }
        },
        frep: {
            options: {
                replacements: [
                    {
                        pattern: /\bXMLHttpRequest\b/g,
                        replacement: 'CouchDroid.NativeXMLHttpRequest'
                    },
                    {
                        pattern: /\blocalStorage\b/g,
                        replacement: 'CouchDroid.fakeLocalStorage'
                    },
                    {
                        pattern: /\bopenDatabase\b/g,
                        replacement: 'CouchDroid.SQLiteNativeDB.openNativeDatabase'
                    }
                ]
            },
            pouchdb: {
                src: '<%= js_lib_path %>/pouchdb-nightly.js',
                dest: '<%= js_build_path %>/pouchdb.js'
            },
            pouchdb_min: {
                src: '<%= js_lib_path %>/pouchdb-nightly.min.js',
                dest: '<%= js_build_path %>/pouchdb_min.js'
            }
        },
        watch: {
            js: {
                files: '<%= js_src_path %>/*.js',
                tasks: ['default']
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-frep');
    grunt.loadNpmTasks('grunt-contrib-watch');

    // Default task.
    grunt.registerTask('default', ['concat', 'uglify', 'frep']);
};
