FROM node:18-alpine AS build

WORKDIR /frontend

COPY package*.json ./
RUN npm install

COPY . .

RUN npm run build

FROM nginx:alpine
COPY --from=build /frontend/build /usr/share/nginx/html

COPY start-nginx.sh .

EXPOSE 80

# CMD ["nginx", "-g", "daemon off;"]
CMD ["./start-nginx.sh"]
