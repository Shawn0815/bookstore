import ApiService from "@/services/ApiService";

const GUEST_CART_KEY = "guestCart";

function recalc(cart) {
  cart.numberOfItems = cart.cartItemList.reduce((sum, i) => sum + i.quantity, 0);
  // ✅ 總計只計入可結帳的：AVAILABLE 與 REACHED_LIMIT
  cart.total = cart.cartItemList
    .filter(i => i.status === "AVAILABLE" || i.status === "REACHED_LIMIT")
    .reduce((sum, i) => sum + i.price * i.quantity, 0);
  return cart;
}

async function validate(cart) {
  for (let item of cart.cartItemList) {
    try {
      const book = await ApiService.fetchBook(item.bookId);

      // 1) 商品不存在 → DISCONTINUED（顯示標記）
      if (!book) {
        item.status = "DISCONTINUED";
        item.message = "商品已下架";
        item.amount = 0;
        continue;
      }

      // 同步最新價格／庫存
      item.price = book.price;
      item.stock = book.stock;

      // 2) 缺貨（庫存 0）→ OUT_OF_STOCK（顯示標記）
      if (book.stock === 0) {
        item.status = "OUT_OF_STOCK";
        item.message = "商品目前缺貨";
        item.amount = 0;
      }
      // 3) 庫存不足（購買量 > 庫存）→ OUT_OF_STOCK（顯示標記）
      else if (item.quantity > book.stock) {
        item.status = "OUT_OF_STOCK";
        item.message = `庫存不足，僅剩 ${book.stock} 本`;
        item.amount = 0; // 不計入金額
      }
      // 4) 達上限（購買量 == 庫存）→ REACHED_LIMIT（顯示標記，可結帳）
      else if (item.quantity === book.stock) {
        item.status = "REACHED_LIMIT";
        item.message = `已達購買上限（最多可購買 ${book.stock} 本）`;
        item.amount = book.price * item.quantity;
      }
      // 5) 正常供應（購買量 < 庫存）→ AVAILABLE（可結帳）
      else {
        item.status = "AVAILABLE";
        item.message = "商品正常販售中";
        item.amount = book.price * item.quantity;
      }
    } catch (e) {
      // 取得失敗當作已下架
      item.status = "DISCONTINUED";
      item.message = "商品已下架";
      item.amount = 0;
    }
  }
  return recalc(cart);
}

export default {
  async getGuestCart() {
    let cart = JSON.parse(localStorage.getItem(GUEST_CART_KEY)) || {
      cartItemList: [],
      total: 0,
      numberOfItems: 0,
    };
    cart = await validate(cart);
    localStorage.setItem(GUEST_CART_KEY, JSON.stringify(cart));
    return cart;
  },

  async addToGuestCart(book, quantity = 1) {
    let cart = JSON.parse(localStorage.getItem(GUEST_CART_KEY)) || {
      cartItemList: [],
      total: 0,
      numberOfItems: 0,
    };

    // 只在「商品不存在」時 alert
    if (!book) {
      alert("書籍不存在或已下架，無法加入購物車");
      return cart;
    }

    const existing = cart.cartItemList.find(i => i.bookId === book.bookId);
    const desiredQuantity = (existing ? existing.quantity : 0) + quantity;

    // 庫存 0：標記為缺貨
    if (book.stock === 0) {
      alert(`商品目前缺貨，無法加入購物車`);
      return cart
    }

    // 超過庫存：自動修正為庫存上限（不 alert；顯示達上限標記）
    if (desiredQuantity > book.stock) {
      if (existing) {
        existing.quantity = book.stock;
      } else {
        cart.cartItemList.push({
          bookId: book.bookId,
          quantity: book.stock,
          title: book.title,
          imageUrl: book.imageUrl,
          price: book.price,
          stock: book.stock,
          amount: book.price * book.stock,
          status: "REACHED_LIMIT",
          message: `已達購買上限（最多可購買 ${book.stock} 本）`,
        });
      }
      cart = await validate(cart);
      localStorage.setItem(GUEST_CART_KEY, JSON.stringify(cart));
      return cart;
    }

    // 正常新增／累加
    if (existing) {
      existing.quantity = desiredQuantity;
    } else {
      cart.cartItemList.push({
        bookId: book.bookId,
        quantity: desiredQuantity,
        title: book.title,
        imageUrl: book.imageUrl,
        price: book.price,
        stock: book.stock,
        amount: book.price * desiredQuantity,
        status: "AVAILABLE",
        message: "商品正常販售中",
      });
    }

    cart = await validate(cart);
    localStorage.setItem(GUEST_CART_KEY, JSON.stringify(cart));
    return cart;
  },

  async updateGuestCart(bookId, quantity) {
    let cart = JSON.parse(localStorage.getItem(GUEST_CART_KEY)) || {
      cartItemList: [],
      total: 0,
      numberOfItems: 0,
    };

    const item = cart.cartItemList.find(i => i.bookId === bookId);
    if (!item) return cart;

    // 移除
    if (quantity <= 0) {
      cart.cartItemList = cart.cartItemList.filter(i => i.bookId !== bookId);
      cart = await validate(cart);
      localStorage.setItem(GUEST_CART_KEY, JSON.stringify(cart));
      return cart;
    }

    // 只在「商品不存在」時 alert；其他僅顯示標記
    const book = await ApiService.fetchBook(bookId);
    if (!book) {
      alert(`書籍「${item.title}」已下架，無法修改數量`);
      // 保留項目供使用者看見標記
      item.status = "DISCONTINUED";
      item.message = "商品已下架";
      item.amount = 0;
      cart = await validate(cart);
      localStorage.setItem(GUEST_CART_KEY, JSON.stringify(cart));
      return cart;
    }

    // 庫存 0：不變更數量（validate 會標 OUT_OF_STOCK）
    if (book.stock === 0) {
      cart = await validate(cart);
      localStorage.setItem(GUEST_CART_KEY, JSON.stringify(cart));
      return cart;
    }

    // 超過庫存：自動修正為上限（顯示達上限）
    if (quantity > book.stock) {
      item.quantity = book.stock;
      // 狀態與金額會由 validate 套用
    } else {
      // 正常更新
      item.quantity = quantity;
    }

    cart = await validate(cart);
    localStorage.setItem(GUEST_CART_KEY, JSON.stringify(cart));
    return cart;
  },

  async deleteGuestCartItem(bookId) {
    let cart = JSON.parse(localStorage.getItem(GUEST_CART_KEY)) || {
      cartItemList: [],
      total: 0,
      numberOfItems: 0,
    };
    cart.cartItemList = cart.cartItemList.filter(i => i.bookId !== bookId);
    cart = await validate(cart);
    localStorage.setItem(GUEST_CART_KEY, JSON.stringify(cart));
    return cart;
  },

  clearGuestCart() {
    localStorage.removeItem(GUEST_CART_KEY);
  },
};
