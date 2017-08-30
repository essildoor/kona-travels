package org.iofstorm.konatravels;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.iofstorm.konatravels.model.Gender;
import org.iofstorm.konatravels.model.Location;
import org.iofstorm.konatravels.model.LocationValidator;
import org.iofstorm.konatravels.model.Mark;
import org.iofstorm.konatravels.model.ShortVisits;
import org.iofstorm.konatravels.model.User;
import org.iofstorm.konatravels.model.UserValidator;
import org.iofstorm.konatravels.model.Visit;
import org.iofstorm.konatravels.model.VisitValidator;
import org.iofstorm.konatravels.service.LocationService;
import org.iofstorm.konatravels.service.UserService;
import org.iofstorm.konatravels.service.VisitService;
import org.rapidoid.RapidoidThing;
import org.rapidoid.buffer.Buf;
import org.rapidoid.bytes.BytesUtil;
import org.rapidoid.http.HttpResponseCodes;
import org.rapidoid.http.MediaType;
import org.rapidoid.http.impl.HttpParser;
import org.rapidoid.http.impl.lowlevel.HttpIO;
import org.rapidoid.net.Protocol;
import org.rapidoid.net.Server;
import org.rapidoid.net.TCP;
import org.rapidoid.net.abstracts.Channel;
import org.rapidoid.net.impl.RapidoidHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import static org.iofstorm.konatravels.Utils.SC_OK;

public class KonaServer extends RapidoidThing implements Protocol {
    private static final byte[] EMPTY = "{}".getBytes();
    private static final String UTF_8 = "utf-8";

    private final byte[] STATUS_200 = HttpResponseCodes.get(200);

    private final byte[] HTTP_400;
    private final byte[] HTTP_404;
    private final byte[] HTTP_500;

    protected final byte[] SERVER_HDR;

    private final byte[] CONN_CLOSE_HDR = hdr("Connection: close");
    private final byte[] CONN_KEEP_ALIVE = hdr("Connection: keep-alive");
    private final byte[] CONTENT_TYPE_APPLICATION_JSON;

    private final HttpParser HTTP_PARSER = createParser();


    private final UserService userService;
    private final LocationService locationService;
    private final VisitService visitService;
    private final Gson gson;

    public KonaServer(Context context) {
        this.SERVER_HDR = hdr("Server: Kona");
        this.HTTP_400 = fullResp(400, "".getBytes());
        this.HTTP_404 = fullResp(404, "".getBytes());
        this.HTTP_500 = fullResp(500, "".getBytes());

        byte[] ct = "Content-Type: ".getBytes();
        byte[] aj = MediaType.APPLICATION_JSON.getBytes();
        CONTENT_TYPE_APPLICATION_JSON = new byte[ct.length + aj.length + CR_LF.length];
        System.arraycopy(ct, 0, CONTENT_TYPE_APPLICATION_JSON, 0, ct.length);
        System.arraycopy(aj, 0, CONTENT_TYPE_APPLICATION_JSON, ct.length, aj.length);
        for (int i = 0; i < CR_LF.length; i++) CONTENT_TYPE_APPLICATION_JSON[ct.length + aj.length + i] = CR_LF[i];

        this.userService = context.getUserService();
        this.locationService = context.getLocationService();
        this.visitService = context.getVisitService();
        this.gson = context.getGson();
    }

    public HttpStatus handle(Channel ctx, Buf buf, RapidoidHelper data) {
        final String path = BytesUtil.get(buf.bytes(), data.path);
        if (path.startsWith("/users")) return handleUsers(ctx, buf, data, path);
        if (path.startsWith("/locations")) return handleLocations(ctx, buf, data, path);
        if (path.startsWith("/visits")) return handleVisits(ctx, buf, data, path);
        return HttpStatus.ERROR;
    }

    @SuppressWarnings("ConstantConditions")
    private HttpStatus handleUsers(Channel ctx, Buf buf, RapidoidHelper data, String path) {
        if (data.isGet.value) {
            final Integer userId = Utils.extractId(path);

            if (userId == null) return HttpStatus.BAD_REQUEST;
            if (userId == -1) return HttpStatus.NOT_FOUND;

            // GET /users/{id}/visits
            if (path.contains("/visits")) {
                String query = BytesUtil.get(buf.bytes(), data.query);
                ShortVisits userVisits;
                if (!query.isEmpty()) { // query params exist
                    Map<String, String> queryParams = Utils.parseQueryParams(query);
                    if (!UserValidator.validateGetUserVisitsQueryParams(queryParams)) return HttpStatus.BAD_REQUEST;
                    String tmp = queryParams.get(UserValidator.FROM_DATE);
                    long fromDate = tmp != null ? Long.valueOf(tmp) : Long.MIN_VALUE;
                    tmp = queryParams.get(UserValidator.TO_DATE);
                    long toDate = tmp != null ? Long.valueOf(tmp) : Long.MIN_VALUE;
                    String country = queryParams.get(UserValidator.COUNTRY);
                    if (country != null) {
                        try {
                            country = URLDecoder.decode(country, UTF_8);
                        } catch (UnsupportedEncodingException ignored) {
                        }
                    }
                    tmp = queryParams.get(UserValidator.TO_DISTANCE);
                    int toDistance = tmp != null ? Integer.valueOf(tmp) : Integer.MIN_VALUE;
                    userVisits = visitService.getUserVisits(userId, fromDate, toDate, country, toDistance);
                } else { // no query params
                    userVisits = visitService.getUserVisits(userId);
                }

                if (userVisits == null) return HttpStatus.NOT_FOUND;

                byte[] body = gson.toJson(userVisits).getBytes();
                return okKeepAlive(ctx, body);
            } else { // GET /users/{id}
                User user = userService.getUserWithoutLock(userId);

                if (user == null) return HttpStatus.NOT_FOUND;

                byte[] body = gson.toJson(user).getBytes();
                return okKeepAlive(ctx, body);
            }
        } else {
            User user;
            try {
                user = gson.fromJson(BytesUtil.get(buf.bytes(), data.body), User.class);
            } catch (JsonSyntaxException ignored) {
                return HttpStatus.BAD_REQUEST;
            }

            // POST /users/new
            if ("/users/new".equals(path)) {
                if (!UserValidator.validateUserOnCreate(user)) return HttpStatus.BAD_REQUEST;
                int code = userService.createUser(user);

                return code == SC_OK ? okConnClose(ctx, EMPTY) : HttpStatus.BAD_REQUEST;
            } else { // POST /users/{id}
                final Integer userId = Utils.extractId(path);
                if (!UserValidator.validateUserOnUpdate(user)) return HttpStatus.BAD_REQUEST;
                int code = userService.updateUser(userId, user);
                switch (code) {
                    case SC_OK:
                        return okConnClose(ctx, EMPTY);
                    case Utils.SC_NOT_FOUND:
                        return HttpStatus.NOT_FOUND;
                    default:
                        return HttpStatus.BAD_REQUEST;
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private HttpStatus handleLocations(Channel ctx, Buf buf, RapidoidHelper data, String path) {
        if (data.isGet.value) {
            final Integer locationId = Utils.extractId(path);

            if (locationId == null) return HttpStatus.BAD_REQUEST;
            if (locationId == -1) return HttpStatus.NOT_FOUND;

            // GET /locations/{id}/avg
            if (path.contains("/avg")) {
                String query = BytesUtil.get(buf.bytes(), data.query);
                Mark mark;
                if (!query.isEmpty()) {
                    Map<String, String> queryParams = Utils.parseQueryParams(query);
                    if (!LocationValidator.validateGetAvgMarkParams(queryParams)) return HttpStatus.BAD_REQUEST;
                    String tmp = queryParams.get(LocationValidator.FROM_DATE);
                    long fromDate = tmp != null ? Long.valueOf(tmp) : Long.MIN_VALUE;
                    tmp = queryParams.get(LocationValidator.TO_DATE);
                    long toDate = tmp != null ? Long.valueOf(tmp) : Long.MIN_VALUE;

                    tmp = queryParams.get(LocationValidator.FROM_AGE);
                    int fromAge = tmp != null ? Integer.valueOf(tmp) : Integer.MIN_VALUE;
                    tmp = queryParams.get(LocationValidator.TO_AGE);
                    int toAge = tmp != null ? Integer.valueOf(tmp) : Integer.MIN_VALUE;

                    tmp = queryParams.get(LocationValidator.GENDER);
                    Gender gender = Gender.fromString(tmp);
                    mark = locationService.getAverageMark(locationId, fromDate, toDate, fromAge, toAge, gender);
                } else {
                    mark = locationService.getAverageMark(locationId);
                }

                if (mark == null) return HttpStatus.NOT_FOUND;

                byte[] body = gson.toJson(mark).getBytes();
                return okKeepAlive(ctx, body);
            } else { // GET /locations/{id}
                Location location = locationService.getLocationWithoutLock(locationId);
                if (location == null) return HttpStatus.NOT_FOUND;
                byte[] body = gson.toJson(location).getBytes();
                return okKeepAlive(ctx, body);
            }
        } else {
            Location location;
            try {
                location = gson.fromJson(BytesUtil.get(buf.bytes(), data.body), Location.class);
            } catch (JsonSyntaxException ignored) {
                return HttpStatus.BAD_REQUEST;
            }

            // POST /locations/new
            if ("/locations/new".equals(path)) {
                if (!LocationValidator.validateOnCreate(location)) return HttpStatus.BAD_REQUEST;
                int code = locationService.createLocation(location);
                return code == SC_OK ? okConnClose(ctx, EMPTY) : HttpStatus.BAD_REQUEST;
            } else { // POST /locations/{id}
                final Integer locationId = Utils.extractId(path);
                if (!LocationValidator.validateOnUpdate(location)) return HttpStatus.BAD_REQUEST;
                int code = locationService.updateLocation(locationId, location);

                switch (code) {
                    case SC_OK:
                        return okConnClose(ctx, EMPTY);
                    case Utils.SC_NOT_FOUND:
                        return HttpStatus.NOT_FOUND;
                    default:
                        return HttpStatus.BAD_REQUEST;
                }
            }
        }
    }

    private HttpStatus handleVisits(Channel ctx, Buf buf, RapidoidHelper data, String path) {
        if (data.isGet.value) {
            final Integer visitId = Utils.extractId(path);

            if (visitId == null) return HttpStatus.BAD_REQUEST;
            if (visitId == -1) return HttpStatus.NOT_FOUND;

            Visit visit = visitService.getVisitWithoutLock(visitId);

            if (visit == null) return HttpStatus.NOT_FOUND;
            byte[] body = gson.toJson(visit).getBytes();
            return okKeepAlive(ctx, body);
        } else {
            Visit visit;
            try {
                visit = gson.fromJson(BytesUtil.get(buf.bytes(), data.body), Visit.class);
            } catch (JsonSyntaxException ignored) {
                return HttpStatus.BAD_REQUEST;
            }

            // POST /visits/new
            if ("/visits/new".equals(path)) {
                if (!VisitValidator.validateOnCreate(visit)) return HttpStatus.BAD_REQUEST;
                int code = visitService.createVisit(visit);
                return code == SC_OK ? okConnClose(ctx, EMPTY) : HttpStatus.BAD_REQUEST;
            } else { // POST /visits/{id}
                final Integer visitId = Utils.extractId(path);
                if (!visitService.visitExist(visitId)) return HttpStatus.NOT_FOUND;
                if (!VisitValidator.validateOnUpdate(visit)) return HttpStatus.BAD_REQUEST;

                visitService.updateVisit(visitId, visit);
                return okConnClose(ctx, EMPTY);
            }
        }
    }

    private static byte[] hdr(String name) {
        return (name + "\r\n").getBytes();
    }

    private byte[] fullResp(int code, byte[] content) {
        String status = new String(HttpResponseCodes.get(code));

        String resp = status +
                "Content-Length: " + content.length + "\r\n" +
                "\r\n" + new String(content);

        return resp.getBytes();
    }

    private HttpParser createParser() {
        return new HttpParser();
    }

    @Override
    public void process(Channel ctx) {
        if (ctx.isInitial()) {
            return;
        }

        Buf buf = ctx.input();
        RapidoidHelper data = ctx.helper();

        HTTP_PARSER.parse(buf, data);

        boolean keepAlive = data.isKeepAlive.value;

        HttpStatus status = handle(ctx, buf, data);

        switch (status) {
            case DONE:
                ctx.closeIf(!keepAlive);
                break;

            case BAD_REQUEST:
                ctx.write(HTTP_400);
                ctx.closeIf(!keepAlive);
                break;

            case NOT_FOUND:
                ctx.write(HTTP_404);
                ctx.closeIf(!keepAlive);
                break;

            case ERROR:
                ctx.write(HTTP_500);
                ctx.closeIf(!keepAlive);
                break;
        }
    }

    private void startResponseKeepAlive(Channel ctx) {
        ctx.write(STATUS_200);
        ctx.write(CONN_KEEP_ALIVE);
        ctx.write(SERVER_HDR);
    }

    private void startResponseConnClose(Channel ctx) {
        ctx.write(STATUS_200);
        ctx.write(CONN_CLOSE_HDR);
        ctx.write(SERVER_HDR);
    }

    private void writeContentTypeHeader(Channel ctx) {
        ctx.write(CONTENT_TYPE_APPLICATION_JSON);
    }

    private void writeBody(Channel ctx, byte[] body) {
        writeContentTypeHeader(ctx);
        HttpIO.INSTANCE.writeContentLengthHeader(ctx, body.length);
        ctx.write(CR_LF);
        ctx.write(body);
    }

    private HttpStatus okKeepAlive(Channel ctx, byte[] body) {
        startResponseKeepAlive(ctx);
        writeBody(ctx, body);
        return HttpStatus.DONE;
    }

    private HttpStatus okConnClose(Channel ctx, byte[] body) {
        startResponseConnClose(ctx);
        writeBody(ctx, body);
        return HttpStatus.DONE;
    }

    Server listen(int port) {
        return listen("0.0.0.0", port);
    }

    Server listen(String address, int port) {
        return TCP.server()
                .protocol(this)
                .address(address)
                .port(port)
                .syncBufs(true)
                .build()
                .start();
    }

    enum HttpStatus {
        DONE, BAD_REQUEST, NOT_FOUND, ERROR
    }
}
