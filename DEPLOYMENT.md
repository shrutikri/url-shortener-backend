# Deploying the URL Shortener Backend (Java Spring Boot)

Since this is a full-fledged Spring Boot backend requiring JVM hosting, Vercel is not an ideal host. Instead, **Render.com** or **Fly.io** provides an excellent free tier for Web Services.

### Deployment via Render.com (Recommended)
1. **Push your code to GitHub:**
   - Initialize a git repository in this `url-shortener-backend` folder.
   - Push it to a new GitHub repository named `url-shortener-backend`.

2. **Connect to Render:**
   - Log in to [Render.com](https://render.com/) and create a new **"Web Service"**.
   - Connect it to your `url-shortener-backend` repository.

3. **Configure the Service:**
   - **Environment:** `Docker` or `Java`. If Java is available organically, select it.
   - **Build Command:** `./gradlew build -x test`
   - **Start Command:** `java -jar build/libs/urlshortener-0.0.1-SNAPSHOT.jar`

4. **Redis Integration (Optional for local testing, mandatory for high load):**
   - Head to [Upstash](https://upstash.com/) and create a free Serverless Redis database.
   - Get the Redis Host and Port.
   - In your Render Web Service, add Environment Variables:
     - `SPRING_DATA_REDIS_HOST` = (your upstash endpoint)
     - `SPRING_DATA_REDIS_PORT` = (your upstash port)

5. **Deploy:** Click Create Web Service. Once live, Render will give you a backend URL (e.g., `https://url-shortener-api.onrender.com`).

*Don't forget to update the `API_BASE_URL` inside your React frontend (`App.jsx`) to point to your new live backend URL before deploying the frontend to Vercel!*
