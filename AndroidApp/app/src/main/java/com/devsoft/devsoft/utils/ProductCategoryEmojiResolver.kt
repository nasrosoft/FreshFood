package com.devsoft.devsoft.utils

object ProductCategoryEmojiResolver {

    /**
     * Resolves the most appropriate food/product emoji based on the product name in
     * English, French, Arabic, and Algerian dialect.
     */
    fun resolveEmoji(productName: String?): String {
        if (productName.isNullOrBlank()) return "📦"

        val normalized = normalize(productName)

        return when {
            // Dairy & Milk
            matches(normalized, "milk", "lait", "حليب", "لبن", "candia", "soummam lait", "loya") -> "🥛"
            matches(normalized, "yogurt", "yaourt", "ياغورت", "زبادي", "دانون", "danone", "activia", "soummam", "yop", "petit suisse") -> "🥣"
            matches(normalized, "cheese", "fromage", "جبن", "جبنة", "فرماج", "gouda", "camembert", "cheddar", "mozzarella", "edam", "gruyere", "brie", "ricotta", "feta", "kiri", "vache qui rit", "la vache", "tartino", "tartiner") -> "🧀"
            matches(normalized, "butter", "beurre", "زبدة", "مارغرين", "margarine", "sol", "fleurial", "laban", "smen", "سمن") -> "🧈"
            matches(normalized, "cream", "creme", "قشطة", "كريمة", "creme fraiche", "chantilly") -> "🥛"

            // Bakery & Grains
            matches(normalized, "bread", "pain", "خبز", "baguette", "باجيت", "toast", "brioche", "mie", "boulangerie", "galette", "kesra", "كسرة", "مطلوع", "matlouh") -> "🥖"
            matches(normalized, "croissant", "كرواسون", "viennoiserie", "pain au chocolat") -> "🥐"
            matches(normalized, "cookie", "cookies", "biscuit", "biscuits", "بسكويت", "قاطو", "bimo", "gaufrette", "wafer", "sable") -> "🍪"
            matches(normalized, "cake", "gateau", "gateaux", "كعكة", "حلوى", "patisserie", "tarte", "muffin", "brownie", "cupcake") -> "🍰"
            matches(normalized, "donut", "doughnut", "beignet", "دونات", "سفنج", "sfenj") -> "🍩"
            matches(normalized, "rice", "riz", "ارز", "أرز", "روز", "basmati", "etuve") -> "🍚"
            matches(normalized, "pasta", "pates", "مكرونة", "مقرونة", "معكرونة", "spaghetti", "سباغيتي", "macaroni", "penne", "tagliatelle", "vermicelle", "couscous", "كسكسي", "كسكس", "berkoukes", "tlitli", "rechta") -> "🍝"
            matches(normalized, "flour", "farine", "طحين", "فرينة", "semoule", "سميد", "levure", "خميرة") -> "🌾"

            // Meat, Poultry & Seafood
            matches(normalized, "chicken", "poulet", "دجاج", "دجاجة", "جاج", "dinde", "escalope", "cuisse", "ailes", "nuggets") -> "🍗"
            matches(normalized, "meat", "viande", "لحم", "بقر", "boeuf", "steak", "veau", "agneau", "خروف", "viande hachee", "كفتة", "viande bovine", "merguez", "مرقاز") -> "🥩"
            matches(normalized, "fish", "poisson", "سمك", "حوت", "saumon", "dorade", "merlan", "espadon", "crevette", "shrimp", "calamar", "seafood", "fruits de mer", "crustace") -> "🐟"
            matches(normalized, "tuna", "thon", "تونة", "sardine", "sardines", "سردين", "anchois", "طون") -> "🥫"
            matches(normalized, "egg", "eggs", "oeuf", "oeufs", "بيض", "عظام", "بيضة") -> "🥚"

            // Fruits
            matches(normalized, "apple", "pomme", "pommes", "تفاح") -> "🍎"
            matches(normalized, "banana", "banane", "bananes", "موز") -> "🍌"
            matches(normalized, "orange", "oranges", "برتقال", "تشينة", "mandarine", "clementine", "mandarines", "يوسفي", "ماندارين") -> "🍊"
            matches(normalized, "strawberry", "fraise", "fraises", "فراولة") -> "🍓"
            matches(normalized, "lemon", "citron", "citrons", "ليمون", "قارص") -> "🍋"
            matches(normalized, "watermelon", "pasteque", "بطيخ", "دلاع", "pasteques") -> "🍉"
            matches(normalized, "melon", "شمام", "cantaloup", "مشمش", "abricot") -> "🍈"
            matches(normalized, "grape", "grapes", "raisin", "raisins", "عنب") -> "🍇"
            matches(normalized, "peach", "peche", "peches", "خوخ") -> "🍑"
            matches(normalized, "cherry", "cherries", "cerise", "cerises", "كرز", "حب الملوك") -> "🍒"
            matches(normalized, "pineapple", "ananas", "اناناس", "أناناس") -> "🍍"
            matches(normalized, "avocado", "avocat", "افوكادو", "أفوكادو") -> "🥑"
            matches(normalized, "dates", "datte", "dattes", "تمر", "دقلة", "deglet nour") -> "🌴"

            // Vegetables
            matches(normalized, "tomato", "tomate", "tomates", "طماطم", "طوماطيش", "sauce tomate", "concentre de tomate", "coulis") -> "🍅"
            matches(normalized, "potato", "pomme de terre", "pommes de terre", "بطاطا", "بطاطس", "frites", "patate") -> "🥔"
            matches(normalized, "carrot", "carotte", "carottes", "جزر", "زرودية") -> "🥕"
            matches(normalized, "onion", "oignon", "oignons", "بصل", "échalote") -> "🧅"
            matches(normalized, "garlic", "ail", "ثوم") -> "🧄"
            matches(normalized, "lettuce", "salade", "laitue", "خس", "سلطة") -> "🥬"
            matches(normalized, "cucumber", "concombre", "concombres", "خيار") -> "🥒"
            matches(normalized, "pepper", "poivron", "poivrons", "فلفل", "طرشي", "piment", "harissa", "هريسة") -> "🫑"
            matches(normalized, "corn", "mais", "maïs", "ذرة") -> "🌽"
            matches(normalized, "olive", "olives", "زيتون") -> "🫒"
            matches(normalized, "mushroom", "champignon", "champignons", "فطر") -> "🍄"

            // Drinks & Beverages
            matches(normalized, "juice", "jus", "عصير", "nectar", "rouiba", "rami", "ifri jus", "toudja", "ngoussa") -> "🧃"
            matches(normalized, "water", "eau", "ماء", "eau minerale", "eau de source", "ifri", "goudelal", "saida", "lalla khedidja", "nestle pure", "hayat", "batna") -> "💧"
            matches(normalized, "soda", "boisson gazeuse", "coca", "coca cola", "pepsi", "fanta", "sprite", "mirinda", "seven up", "7up", "gazouz", "قازوز", "مشروب غازي", "hamoud", "selecto", "slim", "crush", "boga") -> "🥤"
            matches(normalized, "coffee", "cafe", "café", "قهوة", "nescafe", "espresso", "cappuccino", "facto", "bonal", "famico", "aroma") -> "☕"
            matches(normalized, "tea", "the", "thé", "شاي", "تاي", "lipton", "el assil", "infusion", "tisane") -> "🍵"
            matches(normalized, "energy drink", "red bull", "boisson energisante", "monster", "power horse") -> "⚡"

            // Snacks & Sweets
            matches(normalized, "chocolate", "chocolat", "chocolats", "شوكولاتة", "شوكولا", "nutella", "el mordjene", "el wejdene", "bimo choc", "maruja", "milka", "kinder", "twix", "snickers", "kitkat") -> "🍫"
            matches(normalized, "candy", "bonbon", "bonbons", "حلوى", "سكاكر", "caramel", "lollipop", "sucette", "chewing gum", "bubble gum", "clorets", "hollywood") -> "🍬"
            matches(normalized, "ice cream", "glace", "glaces", "ايس كريم", "آيس كريم", "مثلجات", "creme glacee", "cornetto", "magnum") -> "🍦"
            matches(normalized, "chips", "crisps", "شيبس", "بطاطس شيبس", "lays", "doritos", "pringles", "mahboul", "bravas") -> "🥔"
            matches(normalized, "nuts", "cacahuetes", "amandes", "noix", "مكسرات", "كاوكاو", "لوز", "جوز", "فستق", "pistache", "noisette") -> "🥜"
            matches(normalized, "popcorn", "pop corn", "فشار") -> "🍿"
            matches(normalized, "honey", "miel", "عسل") -> "🍯"
            matches(normalized, "jam", "confiture", "مربى", "معجون") -> "🍓"

            // Meals & Fast Food
            matches(normalized, "pizza", "بيتزا") -> "🍕"
            matches(normalized, "burger", "hamburger", "برغر", "برجر") -> "🍔"
            matches(normalized, "sandwich", "sandwiches", "سندويش", "ساندويتش", "panini", "tacos", "shawarma", "شاورما", "كباب", "kebab") -> "🥪"
            matches(normalized, "soup", "soupe", "شوربة", "شربة", "حريرة", "chorba", "harira", "potage") -> "🍲"

            // Condiments & Pantry
            matches(normalized, "oil", "huile", "زيت", "huile d'olive", "huile de tournesol", "elior", "afia", "cevital", "isly") -> "🫒"
            matches(normalized, "vinegar", "vinaigre", "خل") -> "🍾"
            matches(normalized, "salt", "sel", "ملح") -> "🧂"
            matches(normalized, "sugar", "sucre", "سكر") -> "🍬"
            matches(normalized, "mayo", "mayonnaise", "مايونيز", "ketchup", "كاتشب", "mustard", "moutarde", "خردل") -> "🥫"
            matches(normalized, "spice", "spices", "epice", "epices", "بهارات", "توابل", "راس الحانوت", "poivre", "فلفل اكحل", "كمون", "cumin") -> "🧂"

            // Cleaning & Household / Hygiène
            matches(normalized, "soap", "savon", "صابون", "gel douche", "shampoo", "shampoing", "شامبو", "dentifrice", "معجون اسنان") -> "🧼"
            matches(normalized, "detergent", "lessive", "omoo", "ariel", "isis", "lavage", "javel", "جافيل", "clorox", "nettoyant") -> "🧴"
            matches(normalized, "tissue", "mouchoir", "essuie tout", "papier toilette", "مناديل", "ورق صحي") -> "🧻"

            else -> "📦"
        }
    }

    private fun normalize(input: String): String {
        var text = input.lowercase().trim()
        
        // Remove French accents
        text = text.replace("é", "e")
            .replace("è", "e")
            .replace("ê", "e")
            .replace("ë", "e")
            .replace("à", "a")
            .replace("â", "a")
            .replace("ô", "o")
            .replace("ù", "u")
            .replace("û", "u")
            .replace("ç", "c")
            .replace("ï", "i")
            .replace("î", "i")

        // Standardize Arabic Alif & Hamza
        text = text.replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")

        // Remove Arabic Diacritics (Tashkeel)
        text = text.replace(Regex("[\u064B-\u0652]"), "")

        return text
    }

    private fun matches(normalized: String, vararg keywords: String): Boolean {
        for (kw in keywords) {
            val normKw = normalize(kw)
            // Word boundary match or contains match
            if (normalized.contains(normKw)) {
                return true
            }
        }
        return false
    }
}
