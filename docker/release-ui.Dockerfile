FROM node:22-alpine AS build
WORKDIR /ui
COPY release-ui/package*.json ./
RUN npm ci
COPY release-ui ./
RUN npm run build

FROM nginx:alpine
COPY --from=build /ui/dist /usr/share/nginx/html
EXPOSE 80
