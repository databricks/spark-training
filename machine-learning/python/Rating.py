class Rating:
    """
    A wrapper over (user, product, rating) tuple.
    """
    def __init__(self, user, product, rating):
        self.user = int(user)
        self.product = int(product)
        self.rating = float(rating)
