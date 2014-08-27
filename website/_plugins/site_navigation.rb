module Jekyll

  class SiteNavigation < Jekyll::Generator
    safe true
    priority :lowest

    def generate(site)

      # First remove all invisible items (default: nil = show in nav)
      unsorted = []
      site.pages.each do |page|
        if not page.data["navigation"].nil?
          if not page.data["navigation"]["show"].nil?
            unsorted << page if page.data["navigation"]["show"] != false
          else
            puts "no show under nav on " + page.data["title"]
          end
        else
          puts "no nav on page " + page.data["title"]
        end
      end

      # Then sort em according to weight
      sorted = unsorted.sort{ |a,b| a.data["navigation"]["weight"] <=> b.data["navigation"]["weight"] }

      # Debug info.
      puts "Sorted resulting navigation:  (use site.config['sorted_navigation']) "
      sorted.each do |p|
        puts p.inspect
      end

      # Access this in Liquid using: site.navigation
      site.config["navigation"] = sorted
    end
  end
end