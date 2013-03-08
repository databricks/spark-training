require 'fileutils'
include FileUtils

if ENV['SKIP_LESSC'] != '1'
  if system("which lessc > /dev/null 2>&1")
    cd("css")

    Dir.foreach('.') do |file|
      if file.end_with?(".less")
        puts "Running lessc " + file + " > " + file + ".css from " + pwd
        `lessc #{file} > #{file.gsub(".less","")}.css`
      end
    end

    puts "Moving back into root dir."
    cd("..")
  else
    puts "lessc does not appear to be installed on this machine."
  end
end
