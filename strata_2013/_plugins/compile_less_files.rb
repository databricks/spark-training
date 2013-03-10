require 'fileutils'
include FileUtils

if ENV['SKIP_LESSC'] != '1'
  if system("which lessc > /dev/null 2>&1")
    cd("less")

    Dir.foreach('.') do |file|
      if file.end_with?(".less")
        puts "Running lessc " + file + " > ../css/" + file.gsub(".less","") + ".css from " + pwd
        `lessc #{file} > ../css/#{file.gsub(".less","")}.css`
      end
    end

    puts "Moving back into root dir."
    cd("..")
  else
    puts "lessc does not appear to be installed on this machine."
  end
end
