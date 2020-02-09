module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    clean: {
      dist: {
        src: ['dist']
      }
    },
    concat: {
      js: {
        options: {
          separator: ';'
        },
        src: ['src/javascripts/jquery.js','src/javascripts/jquery.*.js', '!src/javascripts/jquery.less.js',
          'src/javascripts/r.main.js', 'src/javascripts/r.*.js'],
        dest: 'dist/reader.js'
      },
      css: {
        src: ['src/stylesheets/*.css', 'dist/less.css'],
        dest: 'dist/reader.css'
      }
    },
    less: {
      dist: {
        src: ['src/stylesheets/main.less'],
        dest: 'dist/less.css'
      },
      theme_default: {
        src: ['src/stylesheets/theme/default.less'],
        dest: 'dist/stylesheets/theme/default.css'
      },
      theme_highcontrast: {
        src: ['src/stylesheets/theme/highcontrast.less'],
        dest: 'dist/stylesheets/theme/highcontrast.css'
      },
      theme_dark: {
        src: ['src/stylesheets/theme/dark.less'],
        dest: 'dist/stylesheets/theme/dark.css'
      }
    },
    cssmin: {
      dist: {
        src: 'dist/reader.css',
        dest: 'dist/stylesheets/reader.min.css'
      }
    },
    uglify: {
      dist: {
        src: 'dist/reader.js',
        dest: 'dist/reader.min.js'
      }
    },
    copy: {
      dist: {
        expand: true,
        cwd: 'src/',
        src: ['**', '!javascripts/*.js', '!index.html', '!**/*.less', '!**/*.css'],
        dest: 'dist/'
      }
    },
    htmlrefs: {
      dist: {
        src: 'src/index.html',
        dest: 'dist/index.html'
      }
    },
    remove: {
      dist: {
        fileList: ['dist/reader.css', 'dist/reader.js', 'dist/less.css'],
        dirList: ['dist/javascripts']
      }
    },
    cleanempty: {
      options: {
        files: false,
        folders: true
      },
      src: ['dist/**']
    },
    replace: {
      dist: {
        src: ['dist/reader.min.js'],
        overwrite: true,
        replacements: [{
          from: '../api',
          to: grunt.option('apiurl') || '../api'
        }]
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-cleanempty');
  grunt.loadNpmTasks('grunt-htmlrefs');
  grunt.loadNpmTasks('grunt-css');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-remove');
  grunt.loadNpmTasks('grunt-text-replace');

  // Default tasks.
  grunt.registerTask('default', ['clean', 'concat:js', 'less', 'concat:css', 'cssmin',
    'uglify', 'copy:dist', 'remove', 'cleanempty', 'htmlrefs', 'replace']);

};